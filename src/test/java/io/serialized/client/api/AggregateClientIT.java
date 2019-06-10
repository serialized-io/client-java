package io.serialized.client.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.google.common.collect.ImmutableList;
import io.dropwizard.testing.junit.DropwizardClientRule;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.aggregate.*;
import io.serialized.client.aggregate.order.OrderPlaced;
import io.serialized.client.aggregate.order.OrderState;
import io.serialized.client.aggregate.order.OrderStatus;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static io.serialized.client.aggregate.AggregateClient.aggregateClient;
import static io.serialized.client.aggregate.EventBatch.newBatch;
import static io.serialized.client.aggregate.order.OrderPlaced.orderPlaced;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class AggregateClientIT {

  private final AggregateApiStub.AggregateApiCallback apiCallback = mock(AggregateApiStub.AggregateApiCallback.class);

  @Rule
  public final DropwizardClientRule DROPWIZARD = new DropwizardClientRule(new AggregateApiStub(apiCallback));

  @Before
  public void setUp() {
    DROPWIZARD.getObjectMapper().setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
  }

  @Test
  public void testLoadAggregateState() throws IOException {

    String aggregateId = "723ecfce-14e9-4889-98d5-a3d0ad54912f";
    String aggregateType = "order";

    AggregateClient<OrderState> orderClient = aggregateClient(aggregateType, OrderState.class, getConfig())
        .registerHandler(OrderPlaced.class, OrderState::orderPlaced)
        .build();

    when(apiCallback.aggregateLoaded(aggregateType, aggregateId)).thenReturn(getResource("/aggregate/load_aggregate.json"));

    OrderState orderState = orderClient.loadState(aggregateId);

    assertThat(orderState.status(), is(OrderStatus.PLACED));
  }

  @Test
  public void loadAggregate() throws IOException {

    AggregateClient<OrderState> orderClient = getOrderClient("order");

    String aggregateId = "723ecfce-14e9-4889-98d5-a3d0ad54912f";
    String aggregateType = "order";

    when(apiCallback.aggregateLoaded(aggregateType, aggregateId)).thenReturn(getResource("/aggregate/load_aggregate_not_classname.json"));

    LoadAggregateResponse aggregateResponse = orderClient.loadEvents(aggregateId);

    assertThat(aggregateResponse.aggregateId(), is(aggregateId));
    assertThat(aggregateResponse.aggregateType(), is(aggregateType));
    assertThat(aggregateResponse.aggregateVersion(), is(1L));
    assertThat(aggregateResponse.events().size(), is(1));
    Event event = aggregateResponse.events().get(0);
    assertNotNull(event.getData());
    assertThat(event.getData().getClass().getSimpleName(), is(OrderPlaced.class.getSimpleName()));
  }

  @Test
  public void testStoreEvents() {

    AggregateClient<OrderState> orderClient = getOrderClient("order");

    UUID aggregateId = UUID.randomUUID();
    orderClient.storeEvents(
        newBatch(aggregateId)
            .addEvent(orderPlaced("order-123", 1234L))
            .build());

    ArgumentCaptor<EventBatch> eventsStoredCaptor = ArgumentCaptor.forClass(EventBatch.class);
    verify(apiCallback).eventsStored(eventsStoredCaptor.capture());

    EventBatch eventsStored = eventsStoredCaptor.getValue();
    assertThat(eventsStored.getAggregateId(), is(aggregateId.toString()));
    List<Event> events = eventsStored.getEvents();
    assertThat(events.size(), is(1));
    Event event = events.get(0);
    assertThat(event.getEventType(), is(OrderPlaced.class.getSimpleName()));
    assertNotNull(event.getData());
  }

  @Test
  public void loadAggregateWithSpecifiedEventType() throws IOException {

    String order = "order";
    String aggregateId = "723ecfce-14e9-4889-98d5-a3d0ad54912f";

    AggregateClient<OrderState> orderClient = getOrderClient(order);

    when(apiCallback.aggregateLoaded(order, aggregateId)).thenReturn(getResource("/aggregate/load_aggregate_not_classname.json"));

    LoadAggregateResponse aggregateResponse = orderClient.loadEvents(aggregateId);

    assertThat(aggregateResponse.aggregateId(), is(aggregateId));
    assertThat(aggregateResponse.aggregateType(), is(order));
    assertThat(aggregateResponse.aggregateVersion(), is(1L));
    assertThat(aggregateResponse.events().size(), is(1));
    assertThat(aggregateResponse.events().get(0).getData().getClass().getSimpleName(), is("OrderPlaced"));
  }

  @Test
  public void storeEventsInBatch() {

    AggregateClient<OrderState> orderClient = getOrderClient("order");

    List<Event> events = ImmutableList.of(orderPlaced("some-test-id-1", 12345));

    EventBatch eventBatch = newBatch("723ecfce-14e9-4889-98d5-a3d0ad54912f", events);

    orderClient.storeEvents(eventBatch);
  }

  @Test
  public void storeSingleEvent() {

    AggregateClient<OrderState> orderClient = getOrderClient("order");

    Event orderPlacedEvent = orderPlaced("ACME Inc.", 12345);

    orderClient.storeEvent("723ecfce-14e9-4889-98d5-a3d0ad54912f", orderPlacedEvent);
  }

  private AggregateClient<OrderState> getOrderClient(String order) {
    return aggregateClient(order, OrderState.class, getConfig())
        .registerHandler("order-placed", OrderPlaced.class, OrderState::orderPlaced)
        .build();
  }

  private SerializedClientConfig getConfig() {
    return SerializedClientConfig.serializedConfig()
        .rootApiUrl(DROPWIZARD.baseUri() + "/api-stub/")
        .accessKey("aaaaa")
        .secretAccessKey("bbbbb").build();
  }

  private String getResource(String resource) throws IOException {
    return IOUtils.toString(getClass().getResourceAsStream(resource), "UTF-8");
  }

}
