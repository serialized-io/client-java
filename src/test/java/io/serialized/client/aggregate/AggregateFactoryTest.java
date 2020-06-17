package io.serialized.client.aggregate;

import io.serialized.client.aggregate.order.Order;
import io.serialized.client.aggregate.order.OrderPlaced;
import io.serialized.client.aggregate.order.OrderState;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.serialized.client.aggregate.AggregateFactory.newFactory;
import static org.assertj.core.api.Assertions.assertThat;

public class AggregateFactoryTest {

  @Test
  public void buildAggregateFromCommands() {
    StateBuilder<OrderState> orderStateBuilder = StateBuilder.stateBuilder(OrderState.class)
        .withHandler(OrderPlaced.class, OrderState::handleOrderPlaced);

    UUID orderId = UUID.randomUUID();

    AggregateFactory<Order, OrderState> orderFactory = newFactory(Order::new, orderStateBuilder);
    Order order = orderFactory.fromCommands(aggregate -> aggregate.placeOrder(orderId, 1000));

    assertThat(order.placeOrder(orderId, 1000)).isEmpty();
    assertThat(order.cancel()).hasSize(1);
  }

}
