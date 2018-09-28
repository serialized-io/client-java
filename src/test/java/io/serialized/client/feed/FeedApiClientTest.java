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

  private FeedApiClient feedClient = FeedApiClient.feedApiClient(
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

    assertThat(feedClient.feeds().feeds().size(), is(1));
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