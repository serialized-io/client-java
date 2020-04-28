package io.serialized.client.feed;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.SerializedOkHttpClient;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.apache.commons.lang3.Validate;

import java.io.Closeable;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class FeedClient implements Closeable {

  private static final String SEQUENCE_NUMBER_HEADER = "Serialized-SequenceNumber-Current";

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

  @Override
  public void close() {
    executors.forEach(ExecutorService::shutdown);
  }

  /**
   * Executes a poll starting at given sequence number.
   *
   * @param since Sequence number to start feeding from. Zero (0) starts from the beginning.
   */
  public FeedResponse execute(GetFeedRequest request, long since) {
    HttpUrl.Builder urlBuilder = url(request.feedName);
    Optional.ofNullable(request.limit).ifPresent(limit -> urlBuilder.addQueryParameter("limit", String.valueOf(limit)));
    HttpUrl url = urlBuilder.addQueryParameter("since", String.valueOf(since)).build();

    if (request.hasTenantId()) {
      return client.get(url, FeedResponse.class, request.tenantId);
    } else {
      return client.get(url, FeedResponse.class);
    }
  }

  /**
   * Executes a poll starting at given sequence number.
   *
   * @param since            Sequence number to start feeding from. Zero (0) starts from the beginning.
   * @param feedEntryHandler Handler invoked for each received entry
   */
  public void execute(GetFeedRequest request, long since, FeedEntryHandler feedEntryHandler) {
    FeedResponse response;
    long offset = since;

    do {
      response = execute(request, offset);
      for (FeedEntry feedEntry : response.entries()) {
        feedEntryHandler.handle(feedEntry);
        offset = feedEntry.sequenceNumber();
      }
    } while (request.eagerFetching && response.hasMore());
  }

  /**
   * Starts subscribing to the feed starting at the beginning.
   *
   * @param feedEntryHandler Handler invoked for each received entry
   */
  public void subscribe(GetFeedRequest request, FeedEntryHandler feedEntryHandler) {
    subscribe(request, 0, feedEntryHandler);
  }

  /**
   * Starts subscribing to the feed starting at given sequence number.
   *
   * @param feedEntryHandler Handler invoked for each received entry
   */
  public void subscribe(GetFeedRequest request, long since, FeedEntryHandler feedEntryHandler) {
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    final AtomicLong offset = new AtomicLong(since);
    executor.scheduleWithFixedDelay(() -> {
      FeedResponse response;

      do {
        response = execute(request, offset.get());
        for (FeedEntry feedEntry : response.entries()) {
          try {
            feedEntryHandler.handle(feedEntry);
            offset.set(feedEntry.sequenceNumber());
          } catch (RetryException e) {
            // Retry requested
          }
        }
      } while (request.eagerFetching && response.hasMore());

    }, request.pollDelay.getSeconds(), request.pollDelay.getSeconds(), TimeUnit.SECONDS);
    executors.add(executor);
  }

  /**
   * @return Feed names and details.
   */
  public List<Feed> execute(ListFeedsRequest request) {
    HttpUrl url = apiRoot.newBuilder().addPathSegment("feeds").build();

    if (request.hasTenantId()) {
      return client.get(url, FeedsResponse.class, request.tenantId).feeds();
    } else {
      return client.get(url, FeedsResponse.class).feeds();
    }
  }

  /**
   * Gets the current sequence number for current feed.
   * <p>
   * Note that the 'all' feed has it's own global sequence.
   *
   * @return The current sequence number, i.e the sequence number of the most recently stored event batch.
   */
  public long execute(GetSequenceNumberRequest request) {
    HttpUrl url = url(request.feedName).build();
    Function<Response, Long> func = response -> Long.parseLong(requireNonNull(response.header(SEQUENCE_NUMBER_HEADER)));

    if (request.hasTenantId()) {
      return client.head(url, func, request.tenantId);
    } else {
      return client.head(url, func);
    }
  }

  private HttpUrl.Builder url(String feedName) {
    Validate.notBlank(feedName, "No feed specified");
    return apiRoot.newBuilder().addPathSegment("feeds").addPathSegment(feedName);
  }

  public static class Builder {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final HttpUrl apiRoot;

    public Builder(SerializedClientConfig config) {
      this.httpClient = config.httpClient();
      this.objectMapper = config.objectMapper();
      this.apiRoot = config.apiRoot();
    }

    public FeedClient build() {
      return new FeedClient(this);
    }

  }

}
