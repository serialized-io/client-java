package io.serialized.client.aggregates.order;

import io.serialized.client.aggregates.Event;

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
