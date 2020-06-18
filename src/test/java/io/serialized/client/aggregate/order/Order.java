package io.serialized.client.aggregate.order;

import io.serialized.client.aggregate.Event;

import java.util.List;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkState;
import static io.serialized.client.aggregate.order.OrderCanceled.orderCanceled;
import static io.serialized.client.aggregate.order.OrderDeleted.orderDeleted;
import static io.serialized.client.aggregate.order.OrderPlaced.orderPlaced;
import static io.serialized.client.aggregate.order.OrderStatus.CANCELED;
import static io.serialized.client.aggregate.order.OrderStatus.DELETED;
import static io.serialized.client.aggregate.order.OrderStatus.NEW;
import static io.serialized.client.aggregate.order.OrderStatus.PLACED;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class Order {

  private final OrderStatus status;
  private final String orderId;

  public Order(OrderState state) {
    this.status = state.status();
    this.orderId = state.orderId();
  }

  public List<Event<?>> placeOrder(UUID orderId, long amount) {
    if (PLACED.equals(status)) {
      return emptyList();
    } else if (NEW.equals(status)) {
      return singletonList(orderPlaced(orderId.toString(), amount));
    } else {
      throw new IllegalStateException("Cannot place order with status: " + status);
    }
  }

  public List<Event<?>> cancel() {
    checkState(orderId != null, "Corrupted state - Id missing!");

    if (CANCELED.equals(status)) {
      return emptyList();
    } else if (DELETED.equals(status)) {
      throw new IllegalStateException("Order is already deleted");
    } else {
      return singletonList(orderCanceled(orderId));
    }
  }

  public List<Event<?>> deleteOrder() {
    checkState(orderId != null, "Corrupted state - Id missing!");

    if (DELETED.equals(status)) {
      return emptyList();
    } else if (CANCELED.equals(status)) {
      return singletonList(orderDeleted(orderId));
    } else {
      throw new IllegalStateException("Only cancelled order can be deleted");
    }
  }

}
