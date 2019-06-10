# README #

This is the Java client for Serialized.

**This is still under development so use at your own risk. APIs and implementation is still subject to change**

Start by registering at [https://serialized.io](https://serialized.io) to get your access key and secret key. You will need them to configure the client.

You will create a separate client instances for the different APIs (aggregates, feeds, projections and reactions).

## Configuring the client
Start by creating a configuration that will used for the client:
```
SerializedClientConfig serializedConfig = SerializedClientConfig.serializedConfig()
      .accessKey("<YOUR-ACCESS-KEY>")
      .secretAccessKey("<YOUR-SECRET-ACCESS-KEY>").build();
```

## Creating an aggregate

To use the aggregate client you need two different classes.

The State implementation and the aggregate root implementation. The state is a mutable class that is used to materialize the current state from the events for a given aggregate type.

The aggregate root implementation is a Java class that contains a number of methods that handles commands. Each method in the aggregate root returns `List<Event>` that should contain `0..N` events. These events will be saved atomically by the client during save.  

You can choose how to organize the command handler methods, but it's recommended to keep them in a single class for each aggregate type.
 
In the following example the `OrderState` class contains the current state of the `Order`, which is the aggregate root:
```
public class OrderState {

  private OrderStatus status;
  private String orderId;

  public OrderState orderPlaced(Event<OrderPlaced> event) {
    this.status = OrderStatus.PLACED;
    this.orderId = event.data().orderId;
    return this;
  }

  public OrderStatus status() {
    return status;
  }

  public String orderId() {
    return orderId;
  }
}
```

```
// Aggregate root Order
public class Order {

  private final OrderStatus status;
  private final String orderId;

  public Order(OrderState state) {
    this.status = state.status();
    this.orderId = state.orderId();
  }

  public List<Event> placeOrder(UUID orderId, long amount) {
    if (orderId.toString().equals(this.orderId)) {
      return emptyList();
    } else {
      return singletonList(orderPlaced(orderId.toString(), amount));
    }
  }

}
```

To create a client handling order events we create a client for the aggregates API by calling  `aggregateClient(String aggregateType, Class<T> stateClass, SerializedClientConfig config)`. We also register each event handler so that the client knows how to materialize the `OrderState` from the stream of events. This must be done for each event type.

### Loading aggregate state from events

```
AggregateClient<OrderState> orderClient = AggregateClient.aggregateClient("order", OrderState.class, serializedConfig)
    .registerHandler(OrderPlaced.class, OrderState::orderPlaced)
    .build();
    
OrderState orderState = orderClient.loadState("<ORDER-AGGREGATE-ID>");

```

### Saving events
When we have loaded the current state we can initialize our aggregate root `Order` using the `OrderState` instance that was created from the call to `loadState()`. The method `placeOrder()`will return the list of events that we want to store and we wrap these in an `EventBatch` to ensure that they will be stored atomically.
```
Order order = new Order(orderState.data());
List<Event> events = order.placeOrder();
orderClient.storeEvents(newBatch(aggregateId, events));
```


## Creating projections
To create projections using the client we need to create a `ProjectionApiClient` by calling `projectionsClient(SerializedClientConfig config)`:
```
ProjectionApiClient projectionsClient = ProjectionApiClient.projectionsClient(serializedConfig).build();
```

There are numerous combinations of projection definitions that you can create and we encourage you to explore the [documentation](https://www.serialized.io/docs/apis/event-projection/) as well as the [sample code](https://github.com/serialized-io/samples-java).

To illustrate we can take the following example which will count the number of placed orders
```
projectionsClient.createOrUpdate(
        aggregatedProjection("order-count")
            .feed("order")
            .addHandler("OrderPlaced", inc("totalPlacedOrders"))
            .build());
```

The `createOrUpdate()` uses the HTTP `PUT` API call which means that this method is is idempotent. A good practice is to place the code above in the initialization part of your service, which will automatically update the projection definition (and re-write all projection data from all events) if the definition code has changed since the last call.

### Querying projections

The projection client also has methods for querying projections that you created. You can create a simple Java class that has the fields needed to deserialize the projection data and pass to the query methods of the projection client.

In this case we have a `OrderCount` class that looks like this:

```
public class OrderCount {
  public int totalPlacedOrders;
}
```

Querying our aggregated projection is now as simple as this:
```
Projection<OrderCount> projectionResponse = projectionApiClient.query(aggregated("order-count").build(OrderCount.class));
OrderCount theCount = projectionResponse.data;
```
