package io.serialized.client.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import io.dropwizard.testing.junit.DropwizardClientRule;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.aggregates.*;
import io.serialized.client.aggregates.order.OrderPlaced;
import io.serialized.client.aggregates.order.OrderState;
import io.serialized.client.aggregates.order.OrderStatus;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static io.serialized.client.aggregates.AggregateClient.aggregateClient;
import static io.serialized.client.aggregates.EventBatch.newBatch;
import static io.serialized.client.aggregates.order.OrderPlaced.orderPlaced;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class AggregateClientIT {

  private static AggregatesApi.Callback apiCallback = mock(AggregatesApi.Callback.class);

  @ClassRule
  public static final DropwizardClientRule DROPWIZARD = new DropwizardClientRule(new AggregatesApi(apiCallback));

  private final SerializedClientConfig serializedConfig = SerializedClientConfig.serializedConfig()
      .rootApiUrl(DROPWIZARD.baseUri() + "/api-stub/")
      .accessKey("aaaaa")
      .secretAccessKey("bbbbb").build();

  private AggregateClient<OrderState> orderClient = aggregateClient("order", OrderState.class, serializedConfig)
      .registerHandler(OrderPlaced.class, OrderState::orderPlaced)
      .build();

  @Before
  public void setUp() {
    DROPWIZARD.getObjectMapper().setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    reset(apiCallback);
  }

  @Test
  public void testLoadAggregateState() {

    AggregateClient<OrderState> build = aggregateClient("order", OrderState.class, serializedConfig)
        .registerHandler(OrderPlaced.class, OrderState::orderPlaced)
        .build();

    State<OrderState> orderState = build.loadState("723ecfce-14e9-4889-98d5-a3d0ad54912f");

    assertThat(orderState.data().status(), is(OrderStatus.PLACED));
  }

  @Test
  public void loadAggregate() {

    LoadAggregateResponse aggregateResponse = orderClient.loadEvents("723ecfce-14e9-4889-98d5-a3d0ad54912f");

    assertThat(aggregateResponse.aggregateId(), is("723ecfce-14e9-4889-98d5-a3d0ad54912f"));
    assertThat(aggregateResponse.aggregateType(), is("order"));
    assertThat(aggregateResponse.aggregateVersion(), is(1L));
    assertThat(aggregateResponse.events().size(), is(1));
    assertThat(aggregateResponse.events().get(0).data().getClass().getSimpleName(), is(OrderPlaced.class.getSimpleName()));
  }

  @Test
  public void testStoreEvents() {

    UUID aggregateId = UUID.randomUUID();
    orderClient.storeEvents(
        newBatch(aggregateId)
            .addEvent(orderPlaced("order-123", 1234L))
            .build());

    ArgumentCaptor<EventBatchDto> eventsStoredCaptor = ArgumentCaptor.forClass(EventBatchDto.class);

    verify(apiCallback).eventsStored(eventsStoredCaptor.capture());

    EventBatchDto eventsStored = eventsStoredCaptor.getValue();
    assertThat(eventsStored.aggregateId, is(aggregateId.toString()));
    assertThat(eventsStored.events.size(), is(1));
    assertThat(eventsStored.events.get(0).eventType, is(OrderPlaced.class.getSimpleName()));
    assertThat(eventsStored.events.get(0).data.get("orderAmount"), is(1234));
    assertThat(eventsStored.events.get(0).data.get("orderId"), is("order-123"));

  }

  @Test
  public void loadAggregateWithSpecificedEventType() {

    AggregateClient<OrderState> aggregatesApiClient = aggregateClient("order-specific", OrderState.class, serializedConfig)
        .registerHandler("order-placed", OrderPlaced.class, OrderState::orderPlaced)
        .build();

    LoadAggregateResponse aggregateResponse = aggregatesApiClient.loadEvents("723ecfce-14e9-4889-98d5-a3d0ad54912f");

    assertThat(aggregateResponse.aggregateId(), is("723ecfce-14e9-4889-98d5-a3d0ad54912f"));
    assertThat(aggregateResponse.aggregateType(), is("order-specific"));
    assertThat(aggregateResponse.aggregateVersion(), is(1L));
    assertThat(aggregateResponse.events().size(), is(1));
    assertThat(aggregateResponse.events().get(0).data().getClass().getSimpleName(), is("OrderPlaced"));
  }

  @Test
  public void storeEventsInBatch() {
    Event<OrderPlaced> orderPlacedEvent = orderPlaced("some-test-id-1", 12345);

    EventBatch eventBatch = newBatch("723ecfce-14e9-4889-98d5-a3d0ad54912f")
        .addEvent(orderPlacedEvent).build();

    orderClient.storeEvents(eventBatch);
  }

  @Test
  public void storeSingleEvent() {

    Event orderPlacedEvent = orderPlaced("ACME Inc.", 12345);

    orderClient.storeEvent("723ecfce-14e9-4889-98d5-a3d0ad54912f", orderPlacedEvent);
  }

}