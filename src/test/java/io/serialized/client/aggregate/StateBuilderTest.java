package io.serialized.client.aggregate;

import io.serialized.client.aggregate.order.OrderCanceled;
import io.serialized.client.aggregate.order.OrderPlaced;
import io.serialized.client.aggregate.order.OrderState;
import io.serialized.client.aggregate.order.OrderStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class StateBuilderTest {

  @Test
  public void testBuildStateFromEvents() {

    StateBuilder<OrderState> orderStateBuilder = StateBuilder.stateBuilder(OrderState.class)
        .withHandler(OrderPlaced.class, OrderState::handleOrderPlaced)
        .withHandler(OrderCanceled.class, OrderState::handleOrderCanceled);

    List<Event<?>> events = asList(
        OrderPlaced.orderPlaced(UUID.randomUUID().toString(), 1000),
        OrderCanceled.orderCanceled(UUID.randomUUID().toString())
    );

    OrderState orderState = orderStateBuilder.buildState(events);

    assertThat(orderState.status()).isEqualTo(OrderStatus.CANCELED);
  }

  @Test
  public void testBuildStateFromEventsOnTopOfCurrentState() {

    StateBuilder<OrderState> orderStateBuilder = StateBuilder.stateBuilder(OrderState.class)
        .withHandler(OrderPlaced.class, OrderState::handleOrderPlaced)
        .withHandler(OrderCanceled.class, OrderState::handleOrderCanceled);

    List<Event<?>> events1 = singletonList(OrderPlaced.orderPlaced(UUID.randomUUID().toString(), 1000));
    OrderState initialState = orderStateBuilder.buildState(events1);
    assertThat(initialState.status()).isEqualTo(OrderStatus.PLACED);

    List<Event<?>> events2 = singletonList(OrderCanceled.orderCanceled(UUID.randomUUID().toString()));
    OrderState endState = orderStateBuilder.buildState(initialState, events2);
    assertThat(endState.status()).isEqualTo(OrderStatus.CANCELED);
  }

}
