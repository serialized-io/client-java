package io.serialized.client.aggregates;

import io.serialized.client.aggregates.order.OrderPlaced;
import io.serialized.client.aggregates.order.OrderState;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static io.serialized.client.aggregates.order.OrderPlaced.orderPlaced;
import static io.serialized.client.aggregates.order.OrderStatus.PLACED;
import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class StateBuilderTest {

  @Test
  public void testBuildStateFromEvents() {

    StateBuilder<OrderState> orderStateBuilder = StateBuilder.stateBuilder(OrderState.class)
        .withHandler(OrderPlaced.class, OrderState::orderPlaced);

    List<Event<OrderPlaced>> events = asList(
        orderPlaced(UUID.randomUUID().toString(), 1000),
        orderPlaced(UUID.randomUUID().toString(), 1000)
    );
    State<OrderState> orderStateState = orderStateBuilder.buildState(events, 2);

    assertThat(orderStateState.data().status(), is(PLACED));
  }
}