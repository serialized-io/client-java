package io.serialized.client.aggregates;

import io.serialized.client.aggregates.order.Order;
import io.serialized.client.aggregates.order.OrderPlaced;
import io.serialized.client.aggregates.order.OrderState;
import org.junit.Test;

import java.util.UUID;

import static io.serialized.client.aggregates.AggregateFactory.newFactory;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class AggregateFactoryTest {

  @Test
  public void buildAggregateFromCommands() {

    StateBuilder<OrderState> orderStateBuilder = StateBuilder.stateBuilder(OrderState.class)
        .withHandler(OrderPlaced.class, OrderState::orderPlaced);

    UUID orderId = UUID.randomUUID();

    AggregateFactory<Order, OrderState> orderFactory = newFactory(Order::new, orderStateBuilder);
    Order order = orderFactory.fromCommands(aggregate -> aggregate.placeOrder(orderId, 1000));

    assertThat(order.placeOrder(orderId, 1000).size(), is(0));
    assertThat(order.placeOrder(UUID.randomUUID(), 1500).size(), is(1));
  }
}