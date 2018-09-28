package io.serialized.client.aggregates;

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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class AggregatesApiClientTest {

  public static class OrderPlacedEvent {

  }

  @Rule
  public MockServerRule mockServerRule = new MockServerRule(this);

  private AggregatesApiClient.Builder aggregatesClientBuilder = AggregatesApiClient.aggregatesApiClient(
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