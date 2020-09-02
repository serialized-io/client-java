# Serialized Java Client #

This is the official Java client for Serialized.

[![Build Status](https://travis-ci.org/serialized-io/client-java.svg?branch=master)](https://travis-ci.org/serialized-io/client-java)
[![Maven Central](https://img.shields.io/maven-central/v/io.serialized/serialized-client.svg)](https://maven-badges.herokuapp.com/maven-central/io.serialized/serialized-client)
[![](https://tokei.rs/b1/github/serialized-io/client-java)](https://github.com/serialized-io/client-java)

## Getting started

Start by registering at [https://serialized.io](https://serialized.io) to get your access key and secret key. You will need them to configure the client.

You will create separate client instances for the different APIs (aggregates, feeds, projections and reactions).

## Adding dependency

Add the following to your Maven POM file

```
<dependency>
  <groupId>io.serialized</groupId>
  <artifactId>serialized-client</artifactId>
  <version>6.8.1</version>
</dependency>
```

## Configuring the client
Start by creating a configuration that will used for the client:
```
SerializedClientConfig serializedConfig = SerializedClientConfig.serializedConfig()
      .accessKey("<YOUR-ACCESS-KEY>")
      .secretAccessKey("<YOUR-SECRET-ACCESS-KEY>").build();
```

## Creating an aggregate

To use the aggregate client you need two different classes.

The *state* implementation and the *aggregate root* implementation. The *state* is a mutable class that is used to
 materialize the current state from the events for a given aggregate type.

The *aggregate root* implementation is a Java class that contains a number of methods that handles commands.
Each method in the aggregate root typically returns `List<Event<?>` that should contain `0..N` events.
These events will be saved atomically by the client during save/update.  

You can choose how to organize the command handler methods, but it's recommended to keep them in a single class for each aggregate type.
 
In the following example the `OrderState` class contains the current *state* of the `Order`, which is the *aggregate root*:
```
public class OrderState {

  private OrderStatus status;
  private String orderId;

  public OrderState handleOrderPlaced(Event<OrderPlaced> event) {
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

  public List<Event<?> placeOrder(UUID orderId, long amount) {
    if (orderId.toString().equals(this.orderId)) {
      return emptyList();
    } else {
      return singletonList(orderPlaced(orderId.toString(), amount));
    }
  }

}
```

To create a client handling order events we create a client for the aggregates API by
calling  `aggregateClient(String aggregateType, Class<T> stateClass, SerializedClientConfig config)`.
We also register each event handler so that the client knows how to materialize the `OrderState` from the stream of events.
This must be done for each event type.

```
AggregateClient<OrderState> orderClient = AggregateClient.aggregateClient("order", OrderState.class, serializedConfig)
    .registerHandler(OrderPlaced.class, OrderState::handleOrderPlaced)
    .build();
```

### Saving events for an aggregate

When we want to save events for a new aggregate we start by initializing our aggregate root `Order` using a
fresh and empty `OrderState` instance. The method `placeOrder()` contains our business logic and will return a list of
events that we want to store. 
The client method `save` will automatically make sure no events are previously stored for our new aggregate (i.e. orderId).
Under the hood this is achieved by including the field *expectedVersion* set to *zero* in the API call to Serialized.

```
OrderState orderState = new OrderState();
Order order = new Order(orderState);
List<Event<?> events = order.placeOrder(orderId, 123L);

orderClient.save(saveRequest().withAggregateId(orderId).withEvents(events).build());
```

### Updating/appending events to an aggregate

When we want to update the aggregate, all previous events have to be loaded and our aggregate root `Order` has to be
fast-forwarded to its current state. For convenience both the loading and the updating part is handled by the client 
method `update`.

```
orderClient.update(orderId, orderState -> {
  Order order = new Order(orderState);
  List<Event<?> events = order.cancel();
  return events;
});
```

The `update` method will automatically handle *optimistic concurrency* and make sure the aggregate is only 
updated if no other change has been performed to it.
Under the hood this is achieved by including the field *expectedVersion* set to the same version number that was
received when the aggregate was first loaded from Serialized.   

## Creating projections
To create projections using the client we need to create a `ProjectionClient` by calling `projectionClient(SerializedClientConfig config)`:
```
ProjectionClient projectionClient = ProjectionClient.projectionClient(serializedConfig).build();
```

There are numerous combinations of projection definitions that you can create and we encourage you to explore 
the [documentation](https://docs.serialized.io/api-reference/apis/projections) as well as
the [sample code](https://github.com/serialized-io/samples-java).

To illustrate we can take the following example which will count the number of placed orders
```
projectionsClient.createOrUpdate(
        aggregatedProjection("order-count")
            .feed("order")
            .addHandler("OrderPlaced",
                inc().with(targetSelector("wins")).build())
            .build());
```

The `createOrUpdate()` uses the HTTP `PUT` API call which means that this method is is idempotent. 
A good practice is to place the code above in the initialization part of your service, which will automatically update
the projection definition (and re-write all projection data from all events) if the definition code has changed since
the last call.

### Querying projections

The projection client also has methods for querying projections that you created. You can create a simple Java class
that has the fields needed to deserialize the projection data and pass to the query methods of the projection client.

In this case we have a `OrderCount` class that looks like this:

```
public class OrderCount {
  public int totalPlacedOrders;
}
```

Querying our aggregated projection is now as simple as this:

```
ProjectionResponse<OrderCount> projectionResponse = projectionClient.query(aggregated("order-count").build(OrderCount.class));
OrderCount theCount = projectionResponse.data;
```

## Defining reactions

Create a client like this:

```
ReactionClient reactionClient = ReactionClient.reactionClient(config).build();
```

Define what action should be triggered given your event:

```
reactionClient.createOrUpdate(newReactionDefinition("new-order-notifier")
        .feed("order")
        .reactOnEventType("OrderPlaced")
        .action(httpAction(
            URI.create("https://yourserver.com/notifications")
            .build()
        ).build());
```

For details, see the [full documentation](https://docs.serialized.io/api-reference/apis/reactions).

## Subscribing to event feeds

Start by creating a `FeedClient` like this:
```
FeedClient feedClient = FeedClient.feedClient(serializedConfig).build();
```

Point out which feed you want to subscribe to and start receiving events for all orders since the beginning of time.

```
GetFeedRequest request = FeedRequests.getFromFeed("customer").build();
feedClient.subscribe(request, feedEntry -> {
  // Process events in entry...
});
```

