package io.serialized.client.aggregate.order;

import io.serialized.client.aggregate.Event;

import static io.serialized.client.aggregate.Event.newEvent;

public class OrderPlaced {

  String orderId;
  long orderAmount;

  public static Event<OrderPlaced> orderPlaced(String orderId, long orderAmount) {
    OrderPlaced orderPlaced = new OrderPlaced();
    orderPlaced.orderId = orderId;
    orderPlaced.orderAmount = orderAmount;
    return newEvent(orderPlaced).build();
  }

}
