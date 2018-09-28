package io.serialized.samples.client;

import io.serialized.samples.client.aggregates.LoadAggregateResponse;
import io.serialized.samples.client.feed.FeedResponse;
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

public class SerializedApiClientTest {

  public static class OrderPlacedEvent {

  }

  @Rule
  public MockServerRule mockServerRule = new MockServerRule(this);

  private SerializedApiClient client = SerializedApiClient.builder()
      .rootApiUrl("http://localhost:" + mockServerRule.getPort())
      .accessKey("aaaaa")
      .secretAccessKey("bbbbb")
      .registerEventType(OrderPlacedEvent.class)
      .build();

  @Before
  public void setUp() throws Exception {
    MockServerClient localhost = new MockServerClient("localhost", mockServerRule.getPort());
    localhost.when(HttpRequest.request("/feeds")).respond(HttpResponse.response().withBody(getResource("feeds.json")));
    localhost.when(HttpRequest.request("/feeds/games")).respond(HttpResponse.response().withBody(getResource("feedentries.json")));
    localhost.when(HttpRequest.request("/aggregates/order/723ecfce-14e9-4889-98d5-a3d0ad54912f")).respond(HttpResponse.response().withBody(getResource("load_aggregate.json")));
  }

  @Test
  public void listFeeds() throws IOException {
    assertThat(client.feedApi().feeds().feeds().size(), is(1));
  }

  @Test
  public void feedEntries() throws IOException {
    FeedResponse feedResponse = client.feedApi().feed("games");
    assertThat(feedResponse.entries().size(), is(48));
    assertThat(feedResponse.events().size(), is(96));
  }

  @Test
  public void loadAggregate() throws IOException {
    LoadAggregateResponse aggregateResponse = client.aggregatesApi().load("order", "723ecfce-14e9-4889-98d5-a3d0ad54912f");
    assertThat(aggregateResponse.aggregateId(), is("723ecfce-14e9-4889-98d5-a3d0ad54912f"));
    assertThat(aggregateResponse.aggregateType(), is("order"));
    assertThat(aggregateResponse.aggregateVersion(), is(1L));
    assertThat(aggregateResponse.events().size(), is(1));
  }

  private String getResource(String s) throws IOException {
    return IOUtils.toString(getClass().getResourceAsStream(s));
  }
}