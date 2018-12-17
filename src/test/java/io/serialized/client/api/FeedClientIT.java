package io.serialized.client.api;

import io.dropwizard.testing.junit.DropwizardClientRule;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.feed.FeedApiStub;
import io.serialized.client.feed.FeedClient;
import io.serialized.client.feed.FeedResponse;
import org.junit.ClassRule;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class FeedClientIT {

  @ClassRule
  public static final DropwizardClientRule DROPWIZARD = new DropwizardClientRule(new FeedApiStub());

  private FeedClient feedClient = FeedClient.feedClient(
      SerializedClientConfig.serializedConfig()
          .rootApiUrl(DROPWIZARD.baseUri() + "/api-stub/")
          .accessKey("aaaaa")
          .secretAccessKey("bbbbb")
          .build())
      .build();

  @Test
  public void listFeeds() {
    assertThat(feedClient.listFeeds().size(), is(1));
    assertThat(feedClient.listFeeds().get(0).aggregateType(), is("games"));
    assertThat(feedClient.listFeeds().get(0).aggregateCount(), is(10L));
    assertThat(feedClient.listFeeds().get(0).batchCount(), is(48L));
    assertThat(feedClient.listFeeds().get(0).eventCount(), is(96L));
  }

  @Test
  public void feedEntries() {
    FeedResponse feedResponse = feedClient.feed("games");

    assertThat(feedResponse.entries().size(), is(48));
    assertThat(feedResponse.events().size(), is(96));
  }
}