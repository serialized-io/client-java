package io.serialized.client.feed;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.SerializedOkHttpClient;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import java.io.Closeable;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.ValueRange;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class FeedClient implements Closeable {

  private static final String SEQUENCE_NUMBER_HEADER = "Serialized-SequenceNumber-Current";
  private static final ValueRange SUBSCRIPTION_POLL_DELAY_VALUE_RANGE = ValueRange.of(1, 60);

  private final SerializedOkHttpClient client;
  private final HttpUrl apiRoot;
  private final Set<ExecutorService> executors = new HashSet<>();

  private FeedClient(Builder builder) {
    this.client = new SerializedOkHttpClient(builder.httpClient, builder.objectMapper);
    this.apiRoot = builder.apiRoot;
  }

  public static Builder feedClient(SerializedClientConfig config) {
    return new Builder(config);
  }

  /**
   * @return Feed names and details.
   */
  public List<Feed> listFeeds() {
    HttpUrl url = apiRoot.newBuilder().addPathSegment("feeds").build();
    return client.get(url, FeedsResponse.class).feeds();
  }

  /**
   * Gets the current project global sequence number.
   *
   * @return The current sequence number, i.e the sequence number of the most recently stored event batch, regardless of feed.
   */
  public long getCurrentGlobalSequenceNumber() {
    HttpUrl url = apiRoot.newBuilder().addPathSegment("feeds").addPathSegment("_all").build();
    return client.head(url, response -> Long.parseLong(requireNonNull(response.header(SEQUENCE_NUMBER_HEADER))));
  }

  /**
   * Gets the current sequence number given a feed.
   *
   * @param feedName Name of feed
   * @return The current sequence number, i.e the sequence number of the most recently stored event batch.
   */
  public long getCurrentSequenceNumber(String feedName) {
    HttpUrl url = apiRoot.newBuilder().addPathSegment("feeds").addPathSegment(feedName).build();
    return client.head(url, response -> Long.parseLong(requireNonNull(response.header(SEQUENCE_NUMBER_HEADER))));
  }

  public FeedRequest feed(String feedName) {
    return new FeedRequest(feedName);
  }

  public FeedRequest all() {
    return new FeedRequest("_all");
  }

  @Override
  public void close() {
    executors.forEach(ExecutorService::shutdown);
  }

  public class FeedRequest {

    private Integer limit;
    private String feedName;
    private Duration pollDelay = Duration.ofSeconds(1);
    private boolean eagerFetching = true;

    private FeedRequest(String feedName) {
      this.feedName = feedName;
    }

    /**
     * @param limit Maximum number of returned feed entries per server response.
     */
    public FeedRequest limit(int limit) {
      this.limit = limit;
      return this;
    }

    /**
     * @param eagerFetching True if the client should continue to fetch event within the same poll as long as there
     *                      are more available. Default is true.
     */
    public FeedRequest eagerFetching(boolean eagerFetching) {
      this.eagerFetching = eagerFetching;
      return this;
    }

    /**
     * @param pollDelay Desired delay between feed polls. Must be between 1s and 60s. Default is 1s.
     */
    public FeedRequest subscriptionPollDelay(Duration pollDelay) {
      if (SUBSCRIPTION_POLL_DELAY_VALUE_RANGE.isValidValue(pollDelay.get(ChronoUnit.SECONDS))) {
        this.pollDelay = pollDelay;
        return this;
      } else {
        throw new IllegalArgumentException(format("Poll delay must be within %d and %d seconds",
            SUBSCRIPTION_POLL_DELAY_VALUE_RANGE.getMinimum(), SUBSCRIPTION_POLL_DELAY_VALUE_RANGE.getMaximum()));
      }
    }

    /**
     * Executes a poll starting at given sequence number.
     *
     * @param since Sequence number to start feeding from. Zero (0) starts from the beginning.
     */
    public FeedResponse execute(long since) {
      return client.get(url().addQueryParameter("since", String.valueOf(since)).build(), FeedResponse.class);
    }

    /**
     * Executes a poll starting at given sequence number.
     *
     * @param since            Sequence number to start feeding from. Zero (0) starts from the beginning.
     * @param feedEntryHandler Handler invoked for each received entry
     */
    public void execute(long since, FeedEntryHandler feedEntryHandler) {
      FeedResponse response;
      long offset = since;

      do {
        response = execute(offset);
        for (FeedEntry feedEntry : response.entries()) {
          feedEntryHandler.handle(feedEntry);
          offset = feedEntry.sequenceNumber();
        }
      } while (eagerFetching && response.hasMore());
    }

    /**
     * Starts subscribing to the feed starting at the beginning.
     *
     * @param feedEntryHandler Handler invoked for each received entry
     */
    public void subscribe(FeedEntryHandler feedEntryHandler) {
      subscribe(0, feedEntryHandler);
    }

    /**
     * Starts subscribing to the feed starting at given sequence number.
     *
     * @param feedEntryHandler Handler invoked for each received entry
     */
    public void subscribe(long since, FeedEntryHandler feedEntryHandler) {
      ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

      final AtomicLong offset = new AtomicLong(since);
      executor.scheduleWithFixedDelay(() -> {
        FeedResponse response;

        do {
          response = execute(offset.get());
          for (FeedEntry feedEntry : response.entries()) {
            try {
              feedEntryHandler.handle(feedEntry);
              offset.set(feedEntry.sequenceNumber());
            } catch (RetryException e) {
              // Retry requested
            }
          }
        } while (eagerFetching && response.hasMore());

      }, pollDelay.getSeconds(), pollDelay.getSeconds(), TimeUnit.SECONDS);
      executors.add(executor);
    }

    private HttpUrl.Builder url() {
      HttpUrl.Builder url = apiRoot.newBuilder().addPathSegment("feeds").addPathSegment(feedName);
      Optional.ofNullable(limit).ifPresent(limit -> url.addQueryParameter("limit", String.valueOf(limit)));
      return url;
    }
  }

  public static class Builder {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final HttpUrl apiRoot;

    Builder(SerializedClientConfig config) {
      this.httpClient = config.httpClient();
      this.objectMapper = config.objectMapper();
      this.apiRoot = config.apiRoot();
    }

    public FeedClient build() {
      return new FeedClient(this);
    }

  }

}
