package io.serialized.client.aggregates.order;

import io.serialized.client.aggregates.Event;

import static io.serialized.client.aggregates.Event.newEvent;

public class OrderPlaced {

  String orderId;
  private long orderAmount;

  public static Event<OrderPlaced> orderPlaced(String orderId, long orderAmount) {
    OrderPlaced orderPlaced = new OrderPlaced();
    orderPlaced.orderId = orderId;
    orderPlaced.orderAmount = orderAmount;
    return newEvent(orderPlaced).build();
  }

}
