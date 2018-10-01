package io.serialized.client.feed;

import io.dropwizard.testing.junit.DropwizardClientRule;
import io.serialized.client.SerializedClientConfig;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class FeedApiClientTest {

  @ClassRule
  public static final DropwizardClientRule DROPWIZARD = new DropwizardClientRule(new FeedApi());

  private FeedApiClient feedClient = FeedApiClient.feedClient(
      SerializedClientConfig.serializedConfig()
          .rootApiUrl(DROPWIZARD.baseUri() + "/api-stub/")
          .accessKey("aaaaa")
          .secretAccessKey("bbbbb")
          .build())
      .build();

  @Test
  public void listFeeds() throws IOException {
    assertThat(feedClient.listFeeds().size(), is(1));
    assertThat(feedClient.listFeeds().get(0).aggregateType(), is("games"));
    assertThat(feedClient.listFeeds().get(0).aggregateCount(), is(10L));
    assertThat(feedClient.listFeeds().get(0).batchCount(), is(48L));
    assertThat(feedClient.listFeeds().get(0).eventCount(), is(96L));
  }

  @Test
  public void feedEntries() throws IOException {
    FeedResponse feedResponse = feedClient.feed("games");

    assertThat(feedResponse.entries().size(), is(48));
    assertThat(feedResponse.events().size(), is(96));
  }
}