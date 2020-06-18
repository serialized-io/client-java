package io.serialized.client.aggregate.order;

import io.serialized.client.aggregate.Event;

import static io.serialized.client.aggregate.Event.newEvent;

public class OrderDeleted {

  String orderId;

  public static Event<OrderDeleted> orderDeleted(String orderId) {
    OrderDeleted orderDeleted = new OrderDeleted();
    orderDeleted.orderId = orderId;
    return newEvent(orderDeleted).build();
  }

}
