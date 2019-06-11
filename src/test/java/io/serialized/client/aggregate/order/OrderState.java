package io.serialized.client.aggregate.order;

import io.serialized.client.aggregate.Event;

import static io.serialized.client.aggregate.order.OrderStatus.CANCELED;
import static io.serialized.client.aggregate.order.OrderStatus.PLACED;

public class OrderState {

  private OrderStatus status = OrderStatus.NEW;
  private String orderId;

  public OrderState handleOrderPlaced(Event<OrderPlaced> event) {
    this.status = PLACED;
    this.orderId = event.getData().orderId;
    return this;
  }

  public OrderState handleOrderCanceled(Event<OrderCanceled> event) {
    this.status = CANCELED;
    return this;
  }

  public String orderId() {
    return orderId;
  }

  public OrderStatus status() {
    return status;
  }

}
