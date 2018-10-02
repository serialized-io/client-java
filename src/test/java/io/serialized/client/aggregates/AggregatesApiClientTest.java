package io.serialized.client.aggregates;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import io.dropwizard.testing.junit.DropwizardClientRule;
import io.serialized.client.SerializedClientConfig;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.UUID;

import static io.serialized.client.aggregates.AggregatesApiClientTest.OrderPlaced.orderPlaced;
import static io.serialized.client.aggregates.Event.newEvent;
import static io.serialized.client.aggregates.EventBatch.newBatch;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class AggregatesApiClientTest {

  public static class OrderPlaced extends Event<OrderPlaced> {

    private String customerId;
    private long orderAmount;

    public static OrderPlaced orderPlaced(String customerId, long orderAmount) {
      OrderPlaced orderPlacedEvent = new OrderPlaced();
      orderPlacedEvent.customerId = customerId;
      orderPlacedEvent.orderAmount = orderAmount;
      return orderPlacedEvent;
    }

  }

  private static AggregatesApi.Callback apiCallback = mock(AggregatesApi.Callback.class);

  @ClassRule
  public static final DropwizardClientRule DROPWIZARD = new DropwizardClientRule(new AggregatesApi(apiCallback));

  private AggregatesApiClient.Builder aggregatesClientBuilder = AggregatesApiClient.aggregatesClient(
      SerializedClientConfig.serializedConfig()
          .rootApiUrl(DROPWIZARD.baseUri() + "/api-stub/")
          .accessKey("aaaaa")
          .secretAccessKey("bbbbb").build());

  @Before
  public void setUp() {
    DROPWIZARD.getObjectMapper().setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    reset(apiCallback);
  }

  @Test
  public void loadAggregate() {
    AggregatesApiClient aggregatesApiClient = aggregatesClientBuilder
        .registerEventType(OrderPlaced.class)
        .build();

    LoadAggregateResponse aggregateResponse = aggregatesApiClient.loadAggregate("order", "723ecfce-14e9-4889-98d5-a3d0ad54912f");

    assertThat(aggregateResponse.aggregateId(), is("723ecfce-14e9-4889-98d5-a3d0ad54912f"));
    assertThat(aggregateResponse.aggregateType(), is("order"));
    assertThat(aggregateResponse.aggregateVersion(), is(1L));
    assertThat(aggregateResponse.events().size(), is(1));
    assertThat(aggregateResponse.events().get(0).data().getClass().getSimpleName(), is(OrderPlaced.class.getSimpleName()));
  }

  @Test
  public void testStoreEvents() {

    AggregatesApiClient aggregatesApiClient = aggregatesClientBuilder.build();

    UUID aggregateId = UUID.randomUUID();
    aggregatesApiClient.storeEvents("order",
        newBatch(aggregateId)
            .addEvent(newEvent(orderPlaced("customer-123", 1234L)).build())
            .build());

    ArgumentCaptor<EventBatchDto> eventsStoredCaptor = ArgumentCaptor.forClass(EventBatchDto.class);

    verify(apiCallback).eventsStored(eventsStoredCaptor.capture());

    EventBatchDto eventsStored = eventsStoredCaptor.getValue();
    assertThat(eventsStored.aggregateId, is(aggregateId.toString()));
    assertThat(eventsStored.events.size(), is(1));
    assertThat(eventsStored.events.get(0).eventType, is(OrderPlaced.class.getSimpleName()));
    assertThat(eventsStored.events.get(0).data.get("orderAmount"), is(1234));
    assertThat(eventsStored.events.get(0).data.get("customerId"), is("customer-123"));

  }

  @Test
  public void loadAggregateWithSpecificedEventType() {
    AggregatesApiClient aggregatesApiClient = aggregatesClientBuilder
        .registerEventType("order-placed", OrderPlaced.class)
        .build();

    LoadAggregateResponse aggregateResponse = aggregatesApiClient.loadAggregate("order-specific", "723ecfce-14e9-4889-98d5-a3d0ad54912f");

    assertThat(aggregateResponse.aggregateId(), is("723ecfce-14e9-4889-98d5-a3d0ad54912f"));
    assertThat(aggregateResponse.aggregateType(), is("order-specific"));
    assertThat(aggregateResponse.aggregateVersion(), is(1L));
    assertThat(aggregateResponse.events().size(), is(1));
    assertThat(aggregateResponse.events().get(0).data().getClass().getSimpleName(), is(OrderPlaced.class.getSimpleName()));
  }

  @Test
  public void storeEventsInBatch() {

    AggregatesApiClient aggregatesApiClient = aggregatesClientBuilder.build();

    Event<OrderPlaced> orderPlacedEvent = newEvent(orderPlaced("some-test-id-1", 12345)).build();

    EventBatch eventBatch = newBatch("723ecfce-14e9-4889-98d5-a3d0ad54912f")
        .addEvent(orderPlacedEvent).build();

    aggregatesApiClient.storeEvents("order", eventBatch);
  }

  @Test
  public void storeSingleEvent() throws IOException {

    AggregatesApiClient aggregatesClient = aggregatesClientBuilder.build();

    Event orderPlacedEvent = newEvent(orderPlaced("ACME Inc.", 12345)).build();

    aggregatesClient.storeEvent("order", "723ecfce-14e9-4889-98d5-a3d0ad54912f", orderPlacedEvent);
  }

}