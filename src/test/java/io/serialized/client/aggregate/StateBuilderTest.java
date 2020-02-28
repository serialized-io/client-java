package io.serialized.client.aggregate;

import io.serialized.client.aggregate.order.OrderPlaced;
import io.serialized.client.aggregate.order.OrderState;
import io.serialized.client.aggregate.order.OrderStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class StateBuilderTest {

  @Test
  public void testBuildStateFromEvents() {

    StateBuilder<OrderState> orderStateBuilder = StateBuilder.stateBuilder(OrderState.class)
        .withHandler(OrderPlaced.class, OrderState::handleOrderPlaced);

    List<Event<OrderPlaced>> events = asList(
        OrderPlaced.orderPlaced(UUID.randomUUID().toString(), 1000),
        OrderPlaced.orderPlaced(UUID.randomUUID().toString(), 1000)
    );

    OrderState orderState = orderStateBuilder.buildState(events);

    assertThat(orderState.status()).isEqualTo(OrderStatus.PLACED);
  }

}
