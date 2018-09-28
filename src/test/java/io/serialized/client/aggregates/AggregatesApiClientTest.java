package io.serialized.client.aggregates;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.testing.junit.DropwizardClientRule;
import io.serialized.client.SerializedClientConfig;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;

import static io.serialized.client.aggregates.AggregatesApiClientTest.OrderPlacedEvent.orderPlaced;
import static io.serialized.client.aggregates.EventBatch.newEvent;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class AggregatesApiClientTest {

  public static class OrderPlacedEvent {
    private String customerId;
    private long orderAmount;

    public static OrderPlacedEvent orderPlaced(String customerId, long orderAmount) {
      OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent();
      orderPlacedEvent.customerId = customerId;
      orderPlacedEvent.orderAmount = orderAmount;
      return orderPlacedEvent;
    }
  }

  @ClassRule
  public static final DropwizardClientRule DROPWIZARD = new DropwizardClientRule(new AggregatesApi());

  private AggregatesApiClient.Builder aggregatesClientBuilder = AggregatesApiClient.aggregatesClient(
      SerializedClientConfig.builder()
          .rootApiUrl(DROPWIZARD.baseUri() + "/api-stub/")
          .accessKey("aaaaa")
          .secretAccessKey("bbbbb").build());

  @Before
  public void setUp() {
    DROPWIZARD.getObjectMapper().setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
  }

  @Test
  public void loadAggregate() throws IOException {
    AggregatesApiClient aggregatesApiClient = aggregatesClientBuilder
        .registerEventType(OrderPlacedEvent.class)
        .build();

    LoadAggregateResponse aggregateResponse = aggregatesApiClient.loadAggregate("order", "723ecfce-14e9-4889-98d5-a3d0ad54912f");

    assertThat(aggregateResponse.aggregateId(), is("723ecfce-14e9-4889-98d5-a3d0ad54912f"));
    assertThat(aggregateResponse.aggregateType(), is("order"));
    assertThat(aggregateResponse.aggregateVersion(), is(1L));
    assertThat(aggregateResponse.events().size(), is(1));
    assertThat(aggregateResponse.events().get(0).data().getClass().getSimpleName(), is(OrderPlacedEvent.class.getSimpleName()));
  }

  @Test
  public void loadAggregateWithSpecificedEventType() throws IOException {
    AggregatesApiClient aggregatesApiClient = aggregatesClientBuilder
        .registerEventType("order-placed", OrderPlacedEvent.class)
        .build();

    LoadAggregateResponse aggregateResponse = aggregatesApiClient.loadAggregate("order-specific", "723ecfce-14e9-4889-98d5-a3d0ad54912f");

    assertThat(aggregateResponse.aggregateId(), is("723ecfce-14e9-4889-98d5-a3d0ad54912f"));
    assertThat(aggregateResponse.aggregateType(), is("order-specific"));
    assertThat(aggregateResponse.aggregateVersion(), is(1L));
    assertThat(aggregateResponse.events().size(), is(1));
    assertThat(aggregateResponse.events().get(0).data().getClass().getSimpleName(), is(OrderPlacedEvent.class.getSimpleName()));
  }

  @Test
  public void storeEventsInBatch() throws IOException {

    AggregatesApiClient aggregatesApiClient = aggregatesClientBuilder.build();

    Event orderPlacedEvent = newEvent("OrderPlaced")
        .eventId("127b80b5-4a05-4774-b870-1c9a2e2a27a3")
        .data(ImmutableMap.of(
            "customerId", "some-test-id-1",
            "orderAmount", 12345))
        .build();

    EventBatch eventBatch = EventBatch.newBatch()
        .aggregateId("723ecfce-14e9-4889-98d5-a3d0ad54912f")
        .addEvent(orderPlacedEvent).build();

    aggregatesApiClient.storeEvents("order", eventBatch);
  }

  @Test
  public void storeSingleEvent() throws IOException {

    AggregatesApiClient aggregatesClient = aggregatesClientBuilder.build();

    Event orderPlacedEvent = EventBatch.newEvent(orderPlaced("ACME Inc.", 12345)).build();

    aggregatesClient.storeEvent("order", "723ecfce-14e9-4889-98d5-a3d0ad54912f", orderPlacedEvent);
  }

}