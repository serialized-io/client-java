package io.serialized.client.feed;

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

public class FeedApiClientTest {

  @Rule
  public MockServerRule mockServerRule = new MockServerRule(this);

  private FeedApiClient feedClient = FeedApiClient.feedClient(
      SerializedClientConfig.builder()
          .rootApiUrl("http://localhost:" + mockServerRule.getPort())
          .accessKey("aaaaa")
          .secretAccessKey("bbbbb")
          .build())
      .build();

  MockServerClient mockServerClient = new MockServerClient("localhost", mockServerRule.getPort());

  @Before
  public void setUp() {
    mockServerClient.reset();
  }

  @Test
  public void listFeeds() throws IOException {
    mockServerClient.when(HttpRequest.request("/feeds")).respond(HttpResponse.response().withBody(getResource("feeds.json")));
    assertThat(feedClient.listFeeds().size(), is(1));
    assertThat(feedClient.listFeeds().get(0).aggregateType(), is("games"));
    assertThat(feedClient.listFeeds().get(0).aggregateCount(), is(10L));
    assertThat(feedClient.listFeeds().get(0).batchCount(), is(48L));
    assertThat(feedClient.listFeeds().get(0).eventCount(), is(96L));
  }

  @Test
  public void feedEntries() throws IOException {
    mockServerClient.when(HttpRequest.request("/feeds/games")).respond(HttpResponse.response().withBody(getResource("feedentries.json")));

    FeedResponse feedResponse = feedClient.feed("games");

    assertThat(feedResponse.entries().size(), is(48));
    assertThat(feedResponse.events().size(), is(96));
  }

  private String getResource(String s) throws IOException {
    return IOUtils.toString(getClass().getResourceAsStream(s));
  }
}