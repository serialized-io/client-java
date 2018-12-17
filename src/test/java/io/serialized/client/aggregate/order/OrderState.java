package io.serialized.client.aggregate.order;

import io.serialized.client.aggregate.Event;

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
