package io.serialized.client.aggregate.order;

import io.serialized.client.aggregate.Event;

import java.util.List;
import java.util.UUID;

import static io.serialized.client.aggregate.order.OrderCanceled.orderCanceled;
import static io.serialized.client.aggregate.order.OrderPlaced.orderPlaced;
import static io.serialized.client.aggregate.order.OrderStatus.CANCELED;
import static io.serialized.client.aggregate.order.OrderStatus.NEW;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class Order {

  private final OrderStatus status;
  private final String orderId;

  public Order(OrderState state) {
    this.status = state.status();
    this.orderId = state.orderId();
  }

  public List<Event> placeOrder(UUID orderId, long amount) {
    if (NEW.equals(status)) {
      return singletonList(orderPlaced(orderId.toString(), amount));
    } else {
      return emptyList();
    }
  }

  public List<Event> cancel() {
    if (CANCELED.equals(status)) {
      return emptyList();
    } else {
      return singletonList(orderCanceled(orderId));
    }
  }

}