package io.serialized.client.feed;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.SerializedOkHttpClient;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.apache.commons.lang3.Validate;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static io.serialized.client.feed.FeedRequests.getSequenceNumber;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.logging.Level.WARNING;

public class FeedClient implements Closeable {

  private static final String SEQUENCE_NUMBER_HEADER = "Serialized-SequenceNumber-Current";

  private final Logger logger = Logger.getLogger(getClass().getName());

  private final SerializedOkHttpClient client;
  private final HttpUrl apiRoot;
  private final Map<UUID, ExecutorService> executors = new ConcurrentHashMap<>();

  private FeedClient(Builder builder) {
    this.client = new SerializedOkHttpClient(builder.httpClient, builder.objectMapper);
    this.apiRoot = builder.apiRoot;
  }

  public static Builder feedClient(SerializedClientConfig config) {
    return new Builder(config);
  }

  @Override
  public void close() {
    executors.values().forEach(ExecutorService::shutdown);
  }

  /**
   * Terminates a subscription by shutting down its executor.
   *
   * @param subscriptionId ID of subscription to terminate.
   */
  public void close(UUID subscriptionId) {
    Optional.ofNullable(executors.remove(subscriptionId)).ifPresent(ExecutorService::shutdown);
  }

  /**
   * Executes a poll starting at given sequence number.
   *
   * @param since Sequence number to start feeding from. Zero (0) starts from the beginning.
   */
  public FeedResponse execute(GetFeedRequest request, long since) {
    HttpUrl.Builder urlBuilder = url(request.feedName);
    Optional.ofNullable(request.limit).ifPresent(limit -> urlBuilder.addQueryParameter("limit", String.valueOf(limit)));
    Optional.ofNullable(request.partitionCount).ifPresent(pCount -> urlBuilder.addQueryParameter("partitionCount", String.valueOf(pCount)));
    Optional.ofNullable(request.partitionNumber).ifPresent(pNumber -> urlBuilder.addQueryParameter("partitionNumber", String.valueOf(pNumber)));
    Optional.ofNullable(request.waitTime).ifPresent(waitTime -> urlBuilder.addQueryParameter("waitTime", String.valueOf(waitTime.toMillis())));

    for (String type : request.types) {
      urlBuilder.addQueryParameter("filterType", type);
    }

    HttpUrl url = urlBuilder.addQueryParameter("since", String.valueOf(since)).build();

    if (request.tenantId().isPresent()) {
      return client.get(url, FeedResponse.class, request.tenantId);
    } else {
      return client.get(url, FeedResponse.class);
    }
  }

  /**
   * Starts subscribing to the feed.
   * The default in-memory sequence number tracker will be used.
   *
   * @param feedEntryHandler Handler invoked for each received entry
   * @return The subscription ID
   * @see SequenceNumberTracker
   * @see InMemorySequenceNumberTracker
   */
  public UUID subscribe(GetFeedRequest request, FeedEntryHandler feedEntryHandler) {
    return subscribe(request, new InMemorySequenceNumberTracker(), feedEntryHandler);
  }

  /**
   * Starts subscribing to the feed.
   * The default in-memory sequence number tracker will be used.
   *
   * @param feedEntryBatchHandler Handler invoked for each received batch
   * @return The subscription ID
   * @see SequenceNumberTracker
   * @see InMemorySequenceNumberTracker
   */
  public UUID subscribe(GetFeedRequest request, FeedEntryBatchHandler feedEntryBatchHandler) {
    return subscribe(request, new InMemorySequenceNumberTracker(), feedEntryBatchHandler);
  }

  /**
   * Starts subscribing to the feed.
   *
   * @param feedEntryHandler Handler invoked for each received entry
   * @return The subscription ID
   */
  public UUID subscribe(GetFeedRequest request, SequenceNumberTracker sequenceNumberTracker, FeedEntryHandler feedEntryHandler) {
    Validate.isTrue(request.waitTime.getSeconds() > 0, "'waitTime' in request cannot be zero when subscribing to a feed");
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    if (request.startFromHead) {
      long sequenceNumber = execute(getSequenceNumber().withFeed(request.feedName).build());
      sequenceNumberTracker.updateLastConsumedSequenceNumber(sequenceNumber);
    }

    executor.scheduleWithFixedDelay(() -> {

      FeedResponse response;

      try {
        do {
          long sequenceNumber = sequenceNumberTracker.lastConsumedSequenceNumber();

          response = execute(request, sequenceNumber);

          if (sequenceNumber > sequenceNumberTracker.lastConsumedSequenceNumber()) {
            return; // Tracker was reset during poll - return to poll again.
          }

          if (response.entries().isEmpty() && response.currentSequenceNumber() > sequenceNumber) {
            sequenceNumberTracker.updateLastConsumedSequenceNumber(response.currentSequenceNumber());

          } else {

            for (FeedEntry feedEntry : response.entries()) {
              try {
                feedEntryHandler.handle(feedEntry);

                try {
                  sequenceNumberTracker.updateLastConsumedSequenceNumber(feedEntry.sequenceNumber());
                } catch (RuntimeException re) {
                  logger.log(WARNING, format("Error updating sequence number after processing: %s - last polled number was [%d]", feedEntry, sequenceNumber), re);
                  throw re;
                }

              } catch (RetryException e) {
                // Retry requested
              }
            }
          }
        } while (request.eagerFetching && response.hasMore());
      } catch (Exception exception) {
        logger.log(WARNING, format("Error polling event feed [%s]: %s", request.feedName, exception.getMessage()), exception);

        try {
          Thread.sleep(1000); // sleep before retrying
        } catch (InterruptedException io) {
          // ignore
        }
      }

    }, 1, 1, TimeUnit.MILLISECONDS);

    UUID subscriptionId = UUID.randomUUID();
    executors.put(subscriptionId, executor);
    return subscriptionId;
  }

  /**
   * Starts subscribing to the feed.
   *
   * @param feedEntryBatchHandler Handler invoked for each received batch
   * @return The subscription ID
   */
  public UUID subscribe(GetFeedRequest request, SequenceNumberTracker sequenceNumberTracker, FeedEntryBatchHandler feedEntryBatchHandler) {
    Validate.isTrue(request.waitTime.getSeconds() > 0, "'waitTime' in request cannot be zero when subscribing to a feed");
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    if (request.startFromHead) {
      long sequenceNumber = execute(getSequenceNumber().withFeed(request.feedName).build());
      sequenceNumberTracker.updateLastConsumedSequenceNumber(sequenceNumber);
    }

    executor.scheduleWithFixedDelay(() -> {

      FeedResponse response;

      try {
        do {
          long sequenceNumber = sequenceNumberTracker.lastConsumedSequenceNumber();

          response = execute(request, sequenceNumber);

          if (sequenceNumber > sequenceNumberTracker.lastConsumedSequenceNumber()) {
            return; // Tracker was reset during poll - return to poll again.
          }

          List<FeedEntry> entries = response.entries();

          if (entries.isEmpty()) {
            if (response.currentSequenceNumber() > sequenceNumber) {
              sequenceNumberTracker.updateLastConsumedSequenceNumber(response.currentSequenceNumber());
            }
          } else {
            feedEntryBatchHandler.handle(entries);
            sequenceNumberTracker.updateLastConsumedSequenceNumber(entries.get(entries.size() - 1).sequenceNumber());
          }

        } while (request.eagerFetching && response.hasMore());
      } catch (Exception exception) {
        logger.log(WARNING, format("Error polling event feed [%s]: %s", request.feedName, exception.getMessage()), exception);

        try {
          Thread.sleep(1000); // sleep before retrying
        } catch (InterruptedException io) {
          // ignore
        }
      }

    }, 1, 1, TimeUnit.MILLISECONDS);

    UUID subscriptionId = UUID.randomUUID();
    executors.put(subscriptionId, executor);
    return subscriptionId;
  }

  /**
   * @return Feed names and details.
   */
  public List<Feed> execute(ListFeedsRequest request) {
    HttpUrl url = apiRoot.newBuilder().addPathSegment("feeds").build();

    if (request.tenantId().isPresent()) {
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

    if (request.tenantId().isPresent()) {
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

    private final ObjectMapper objectMapper = new ObjectMapper()
        .disable(FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(FAIL_ON_EMPTY_BEANS)
        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        .setSerializationInclusion(NON_NULL);

    private final OkHttpClient httpClient;
    private final HttpUrl apiRoot;

    public Builder(SerializedClientConfig config) {
      this.httpClient = config.newHttpClient();
      this.apiRoot = config.apiRoot();
    }

    /**
     * Allows object mapper customization.
     */
    public Builder configureObjectMapper(Consumer<ObjectMapper> consumer) {
      consumer.accept(objectMapper);
      return this;
    }

    public FeedClient build() {
      return new FeedClient(this);
    }

  }

}
