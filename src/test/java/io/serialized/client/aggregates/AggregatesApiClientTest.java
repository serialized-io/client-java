package io.serialized.client.aggregates;

import com.google.common.collect.ImmutableMap;
import io.serialized.client.SerializedClientConfig;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import java.io.IOException;
import java.util.UUID;

import static io.serialized.client.aggregates.EventBatch.newEvent;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class AggregatesApiClientTest {

  public static class OrderPlacedEvent {

  }

  @Rule
  public MockServerRule mockServerRule = new MockServerRule(this);

  private AggregatesApiClient.Builder aggregatesClientBuilder = AggregatesApiClient.aggregatesClient(
      SerializedClientConfig.builder()
          .rootApiUrl("http://localhost:" + mockServerRule.getPort())
          .accessKey("aaaaa")
          .secretAccessKey("bbbbb").build());

  MockServerClient mockServerClient = new MockServerClient("localhost", mockServerRule.getPort());

  @Before
  public void setUp() {
    mockServerClient.reset();
  }

  @Test
  public void loadAggregate() throws IOException {
    mockServerClient.when(HttpRequest.request("/aggregates/order/723ecfce-14e9-4889-98d5-a3d0ad54912f")).respond(HttpResponse.response().withBody(getResource("load_aggregate.json")));

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
  public void saveEvents() throws IOException {
    mockServerClient.when(HttpRequest.request("/aggregates/order/events")
        .withBody(getResource("save_events.json"))
        .withMethod("POST"))
        .respond(HttpResponse.response().withStatusCode(200));

    AggregatesApiClient aggregatesApiClient = aggregatesClientBuilder
        .registerEventType(OrderPlacedEvent.class)
        .build();

    EventBatch eventBatch = EventBatch.newBatch()
        .aggregateId(UUID.fromString("723ecfce-14e9-4889-98d5-a3d0ad54912f"))
        .addEvent(newEvent("OrderPlaced")
            .eventId(UUID.fromString("127b80b5-4a05-4774-b870-1c9a2e2a27a3"))
            .data(ImmutableMap.of(
                "customerId", "some-test-id-1",
                "orderAmount", 12345))
            .build()).build();

    aggregatesApiClient.storeEvents("order", eventBatch);
  }

  @Test
  public void loadAggregateWithSpecificedEventType() throws IOException {
    mockServerClient.when(HttpRequest.request("/aggregates/order/723ecfce-14e9-4889-98d5-a3d0ad54912f")).respond(HttpResponse.response().withBody(getResource("load_aggregate_not_classname.json")));

    AggregatesApiClient aggregatesApiClient = aggregatesClientBuilder
        .registerEventType("order-placed", OrderPlacedEvent.class)
        .build();

    LoadAggregateResponse aggregateResponse = aggregatesApiClient.loadAggregate("order", "723ecfce-14e9-4889-98d5-a3d0ad54912f");

    assertThat(aggregateResponse.aggregateId(), is("723ecfce-14e9-4889-98d5-a3d0ad54912f"));
    assertThat(aggregateResponse.aggregateType(), is("order"));
    assertThat(aggregateResponse.aggregateVersion(), is(1L));
    assertThat(aggregateResponse.events().size(), is(1));
    assertThat(aggregateResponse.events().get(0).data().getClass().getSimpleName(), is(OrderPlacedEvent.class.getSimpleName()));
  }

  private String getResource(String s) throws IOException {
    return IOUtils.toString(getClass().getResourceAsStream(s));
  }
}