package io.serialized.client.api;

import io.dropwizard.testing.junit5.DropwizardClientExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.feed.Feed;
import io.serialized.client.feed.FeedApiStub;
import io.serialized.client.feed.FeedClient;
import io.serialized.client.feed.FeedResponse;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
public class FeedClientIT {

  private FeedApiStub.FeedApiCallback apiCallback = mock(FeedApiStub.FeedApiCallback.class);

  public final DropwizardClientExtension dropwizard = new DropwizardClientExtension(new FeedApiStub(apiCallback));

  @Test
  public void listFeeds() throws IOException {

    FeedClient feedClient = getFeedClient();

    when(apiCallback.feedOverviewLoaded()).thenReturn(getResource("/feed/feeds.json"));

    List<Feed> feeds = feedClient.request().listFeeds();
    assertThat(feeds).hasSize(1);
    assertThat(feeds.get(0).aggregateType()).isEqualTo("games");
    assertThat(feeds.get(0).aggregateCount()).isEqualTo(10L);
    assertThat(feeds.get(0).batchCount()).isEqualTo(48L);
    assertThat(feeds.get(0).eventCount()).isEqualTo(96L);
  }

  @Test
  public void getCurrentSequenceNumber() {

    FeedClient feedClient = getFeedClient();

    when(apiCallback.currentSequenceNumberRequested()).thenReturn(7L);

    assertThat(feedClient.feed("games").getCurrentSequenceNumber()).isEqualTo(7L);
  }

  @Test
  public void getCurrentGlobalSequenceNumber() {

    FeedClient feedClient = getFeedClient();

    when(apiCallback.currentGlobalSequenceNumberRequested()).thenReturn(777L);

    assertThat(feedClient.all().getCurrentSequenceNumber()).isEqualTo(777L);
  }

  @Test
  public void allFeedEntries() throws IOException {

    FeedClient feedClient = getFeedClient();

    when(apiCallback.allFeedLoaded()).thenReturn(getResource("/feed/allFeed.json"));

    FeedResponse feedResponse = feedClient.all().execute(0);

    assertThat(feedResponse.entries()).hasSize(1);
    assertThat(feedResponse.events()).hasSize(1);
  }

  @Test
  public void feedEntries() throws IOException {

    FeedClient feedClient = getFeedClient();
    String feedName = "games";

    ArgumentCaptor<FeedApiStub.QueryParams> queryParams = ArgumentCaptor.forClass(FeedApiStub.QueryParams.class);
    when(apiCallback.feedEntriesLoaded(eq(feedName), queryParams.capture())).thenReturn(getResource("/feed/feedentries.json"));

    FeedResponse feedResponse = feedClient.feed(feedName).execute(0);

    assertThat(queryParams.getValue().getLimit()).isEqualTo(1000);
    assertThat(feedResponse.entries()).hasSize(48);
    assertThat(feedResponse.events()).hasSize(96);
  }

  @Test
  public void feedEntriesWithLimit() throws IOException {

    FeedClient feedClient = getFeedClient();
    String feedName = "games";

    int limit = 10;
    ArgumentCaptor<FeedApiStub.QueryParams> queryParams = ArgumentCaptor.forClass(FeedApiStub.QueryParams.class);
    when(apiCallback.feedEntriesLoaded(eq(feedName), queryParams.capture())).thenReturn(getResource("/feed/feedentries-limit.json"));

    FeedResponse feedResponse = feedClient.feed(feedName).limit(limit).execute(0);

    assertThat(queryParams.getValue().getLimit()).isEqualTo(10);
    assertThat(feedResponse.entries()).hasSize(10);
    assertThat(feedResponse.events()).hasSize(20);
  }

  @Test
  public void feedEntriesWithLimitAndSince() throws IOException {

    FeedClient feedClient = getFeedClient();
    String feedName = "games";

    int limit = 10;
    ArgumentCaptor<FeedApiStub.QueryParams> queryParams = ArgumentCaptor.forClass(FeedApiStub.QueryParams.class);
    when(apiCallback.feedEntriesLoaded(eq(feedName), queryParams.capture())).thenReturn(getResource("/feed/feedentries-limit.json"));

    FeedResponse feedResponse = feedClient.feed(feedName).limit(limit).execute(3);

    assertThat(queryParams.getValue().getLimit()).isEqualTo(10);
    assertThat(queryParams.getValue().getSince()).isEqualTo(3L);
    assertThat(feedResponse.entries().size()).isEqualTo(10);
    assertThat(feedResponse.events().size()).isEqualTo(20);
  }

  @Test
  public void feedEntriesWithCallback() throws IOException {

    FeedClient feedClient = getFeedClient();
    String feedName = "games";

    ArgumentCaptor<FeedApiStub.QueryParams> queryParams = ArgumentCaptor.forClass(FeedApiStub.QueryParams.class);
    when(apiCallback.feedEntriesLoaded(eq(feedName), queryParams.capture())).thenReturn(getResource("/feed/feedentries-limit.json"));

    AtomicInteger entries = new AtomicInteger();
    AtomicInteger events = new AtomicInteger();
    AtomicLong lastProcessedEntry = new AtomicLong();

    feedClient.feed(feedName).limit(10).execute(3, feedEntry -> {
      entries.incrementAndGet();
      events.addAndGet(feedEntry.events().size());
      assertNotNull(feedEntry.aggregateId());
      assertTrue(feedEntry.timestamp() > 0L);
      lastProcessedEntry.set(feedEntry.sequenceNumber());
    });

    assertThat(queryParams.getValue().getLimit()).isEqualTo(10);
    assertThat(queryParams.getValue().getSince()).isEqualTo(3L);

    assertThat(entries.get()).isEqualTo(10);
    assertThat(events.get()).isEqualTo(20);

    assertThat(lastProcessedEntry.get()).isEqualTo(13L);
  }

  private FeedClient getFeedClient() {
    return FeedClient.feedClient(
        SerializedClientConfig.serializedConfig()
            .rootApiUrl(dropwizard.baseUri() + "/api-stub/")
            .accessKey("aaaaa")
            .secretAccessKey("bbbbb")
            .build())
        .build();
  }

  private String getResource(String resource) throws IOException {
    return IOUtils.toString(getClass().getResourceAsStream(resource), UTF_8);
  }

}
