package io.serialized.client.api;

import io.dropwizard.testing.junit.DropwizardClientRule;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.feed.Feed;
import io.serialized.client.feed.FeedApiStub;
import io.serialized.client.feed.FeedClient;
import io.serialized.client.feed.FeedResponse;
import org.apache.commons.io.IOUtils;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FeedClientIT {

  private static FeedApiStub.FeedApiCallback apiCallback = mock(FeedApiStub.FeedApiCallback.class);

  @ClassRule
  public static final DropwizardClientRule DROPWIZARD = new DropwizardClientRule(new FeedApiStub(apiCallback));

  private FeedClient feedClient = FeedClient.feedClient(
      SerializedClientConfig.serializedConfig()
          .rootApiUrl(DROPWIZARD.baseUri() + "/api-stub/")
          .accessKey("aaaaa")
          .secretAccessKey("bbbbb")
          .build())
      .build();

  @Test
  public void listFeeds() throws IOException {

    when(apiCallback.feedOverviewLoaded()).thenReturn(getResource("/feed/feeds.json"));

    List<Feed> feeds = feedClient.listFeeds();
    assertThat(feeds.size(), is(1));
    assertThat(feeds.get(0).aggregateType(), is("games"));
    assertThat(feeds.get(0).aggregateCount(), is(10L));
    assertThat(feeds.get(0).batchCount(), is(48L));
    assertThat(feeds.get(0).eventCount(), is(96L));
  }

  @Test
  public void feedEntries() throws IOException {
    String feedName = "games";

    when(apiCallback.feedEntriesLoaded(feedName)).thenReturn(getResource("/feed/feedentries.json"));

    FeedResponse feedResponse = feedClient.feed(feedName);

    assertThat(feedResponse.entries().size(), is(48));
    assertThat(feedResponse.events().size(), is(96));
  }


  private String getResource(String resource) throws IOException {
    return IOUtils.toString(getClass().getResourceAsStream(resource), "UTF-8");
  }

}
