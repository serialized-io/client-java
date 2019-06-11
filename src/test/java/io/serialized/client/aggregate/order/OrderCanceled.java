package io.serialized.client.aggregate.order;

import io.serialized.client.aggregate.Event;

import static io.serialized.client.aggregate.Event.newEvent;

public class OrderCanceled {

  String orderId;

  public static Event<OrderCanceled> orderCanceled(String orderId) {
    OrderCanceled orderCanceled = new OrderCanceled();
    orderCanceled.orderId = orderId;
    return newEvent(orderCanceled).build();
  }

}
