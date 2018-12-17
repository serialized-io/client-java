package io.serialized.client.aggregate;

import io.serialized.client.aggregate.order.OrderPlaced;
import io.serialized.client.aggregate.order.OrderState;
import io.serialized.client.aggregate.order.OrderStatus;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertThat;

public class StateBuilderTest {

  @Test
  public void testBuildStateFromEvents() {

    StateBuilder<OrderState> orderStateBuilder = StateBuilder.stateBuilder(OrderState.class)
        .withHandler(OrderPlaced.class, OrderState::orderPlaced);

    List<Event<OrderPlaced>> events = asList(
        OrderPlaced.orderPlaced(UUID.randomUUID().toString(), 1000),
        OrderPlaced.orderPlaced(UUID.randomUUID().toString(), 1000)
    );

    State<OrderState> orderStateState = orderStateBuilder.buildState(events, 2);

    assertThat(orderStateState.data().status(), Is.is(OrderStatus.PLACED));
  }

}