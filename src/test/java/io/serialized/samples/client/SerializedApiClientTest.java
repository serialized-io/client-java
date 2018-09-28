package io.serialized.samples.client;

import io.serialized.samples.client.aggregates.LoadAggregateResponse;
import io.serialized.samples.client.feed.FeedResponse;
import io.serialized.samples.client.projection.ProjectionApiClient;
import io.serialized.samples.client.projection.ProjectionResponse;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import java.io.IOException;

import static io.serialized.samples.client.projection.ProjectionQuery.singleProjection;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SerializedApiClientTest {

  public static class OrderPlacedEvent {

  }

  public static class OrderBalanceProjection {

    public long orderAmount;

  }

  @Rule
  public MockServerRule mockServerRule = new MockServerRule(this);

  private SerializedApiClient.Builder clientBuilder = SerializedApiClient.builder()
      .rootApiUrl("http://localhost:" + mockServerRule.getPort())
      .accessKey("aaaaa")
      .secretAccessKey("bbbbb");

  MockServerClient mockServerClient = new MockServerClient("localhost", mockServerRule.getPort());

  @Before
  public void setUp() {
    mockServerClient.reset();
  }

  @Test
  public void listFeeds() throws IOException {
    mockServerClient.when(HttpRequest.request("/feeds")).respond(HttpResponse.response().withBody(getResource("feeds.json")));

    SerializedApiClient client = clientBuilder.build();

    assertThat(client.feedApi().feeds().feeds().size(), is(1));
  }

  @Test
  public void feedEntries() throws IOException {
    mockServerClient.when(HttpRequest.request("/feeds/games")).respond(HttpResponse.response().withBody(getResource("feedentries.json")));

    SerializedApiClient client = clientBuilder.build();

    FeedResponse feedResponse = client.feedApi().feed("games");

    assertThat(feedResponse.entries().size(), is(48));
    assertThat(feedResponse.events().size(), is(96));
  }

  @Test
  public void testSingleProjection() throws IOException {
    mockServerClient.when(HttpRequest.request("/projections/single/orders/723ecfce-14e9-4889-98d5-a3d0ad54912f")).respond(HttpResponse.response().withBody(getResource("single_projection.json")));

    ProjectionApiClient client = clientBuilder.build().projectionApi();

    ProjectionResponse<OrderBalanceProjection> projection = client.query(
        singleProjection("orders")
            .id("723ecfce-14e9-4889-98d5-a3d0ad54912f")
            .as(OrderBalanceProjection.class)
            .build()
    );

    assertThat(projection.projectionId(), is("723ecfce-14e9-4889-98d5-a3d0ad54912f"));
    assertThat(projection.updatedAt(), is(1505754083976L));
    assertThat(projection.data().orderAmount, is(12345L));
  }

  @Test
  public void loadAggregate() throws IOException {
    mockServerClient.when(HttpRequest.request("/aggregates/order/723ecfce-14e9-4889-98d5-a3d0ad54912f")).respond(HttpResponse.response().withBody(getResource("load_aggregate.json")));

    SerializedApiClient client = clientBuilder
        .registerEventType(OrderPlacedEvent.class)
        .build();

    LoadAggregateResponse aggregateResponse = client.aggregatesApi().loadAggregate("order", "723ecfce-14e9-4889-98d5-a3d0ad54912f");

    assertThat(aggregateResponse.aggregateId(), is("723ecfce-14e9-4889-98d5-a3d0ad54912f"));
    assertThat(aggregateResponse.aggregateType(), is("order"));
    assertThat(aggregateResponse.aggregateVersion(), is(1L));
    assertThat(aggregateResponse.events().size(), is(1));
    assertThat(aggregateResponse.events().get(0).data().getClass().getSimpleName(), is(OrderPlacedEvent.class.getSimpleName()));
  }

  @Test
  public void loadAggregateWithSpecificedEventType() throws IOException {
    mockServerClient.when(HttpRequest.request("/aggregates/order/723ecfce-14e9-4889-98d5-a3d0ad54912f")).respond(HttpResponse.response().withBody(getResource("load_aggregate_not_classname.json")));

    SerializedApiClient client = clientBuilder
        .registerEventType("order-placed", OrderPlacedEvent.class)
        .build();

    LoadAggregateResponse aggregateResponse = client.aggregatesApi().loadAggregate("order", "723ecfce-14e9-4889-98d5-a3d0ad54912f");

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