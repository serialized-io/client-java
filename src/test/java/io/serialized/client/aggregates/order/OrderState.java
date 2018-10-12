package io.serialized.client.aggregates.order;

import io.serialized.client.aggregates.Event;

public class OrderState {

  public OrderStatus status;
  public String orderId;

  public OrderState orderPlaced(Event<OrderPlaced> event) {
    this.status = OrderStatus.PLACED;
    this.orderId = event.data().orderId;
    return this;
  }

}
