package io.serialized.client.aggregate;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.serialized.client.ApiException;
import io.serialized.client.ConcurrencyException;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.SerializedOkHttpClient;
import io.serialized.client.aggregate.cache.StateCache;
import io.serialized.client.aggregate.cache.VersionedState;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static io.serialized.client.aggregate.StateBuilder.stateBuilder;
import static java.lang.String.format;
import static java.util.logging.Level.INFO;

public class AggregateClient<T> {

  private final Logger logger = Logger.getLogger(getClass().getName());

  private final SerializedOkHttpClient client;
  private final HttpUrl apiRoot;
  private final StateBuilder<T> stateBuilder;
  private final String aggregateType;
  private final RetryStrategy retryStrategy;
  private final int limit;

  private AggregateClient(Builder<T> builder) {
    this.client = new SerializedOkHttpClient(builder.httpClient, builder.objectMapper);
    this.apiRoot = builder.apiRoot;
    this.aggregateType = builder.aggregateType;
    this.stateBuilder = builder.stateBuilder;
    this.retryStrategy = builder.retryStrategy;
    this.limit = builder.limit;
  }

  public static <T> Builder<T> aggregateClient(String aggregateType, Class<T> stateClass, SerializedClientConfig config) {
    return new Builder<>(aggregateType, stateClass, config);
  }

  /**
   * Save or append events to an aggregate according to the given request
   *
   * @param request the request to perform
   */
  public void save(AggregateRequest request) {

    try {
      HttpUrl url = getAggregateUrl(request.aggregateId).addPathSegment("events").build();

      if (request.tenantId().isPresent()) {
        UUID tenantId = request.tenantId().get();
        client.post(url, request.eventBatch(), tenantId);
      } else {
        client.post(url, request.eventBatch());
      }
    } catch (ApiException e) {
      handleConcurrencyException(e);
    }
  }

  /**
   * Update the aggregate.
   * <p>
   * The update will be performed using optimistic concurrency check depending on the
   * {@link AggregateUpdate#useOptimisticConcurrencyOnUpdate} setting.
   *
   * @param aggregateId The ID of the aggregate.
   * @param update      Function that executes business logic and returns the resulting domain events.
   * @return Number of events stored in batch
   */
  public int update(String aggregateId, AggregateUpdate<T> update) {
    return update(UUID.fromString(aggregateId), update);
  }

  /**
   * Update the aggregate.
   * <p>
   * The update will be performed using optimistic concurrency check depending on the
   * {@link AggregateUpdate#useOptimisticConcurrencyOnUpdate} setting.
   *
   * @param aggregateId The ID of the aggregate.
   * @param update      Function that executes business logic and returns the resulting domain events.
   * @return Number of events stored in batch
   */
  public int update(UUID aggregateId, AggregateUpdate<T> update) {

    ConcurrencyException lastException = new ConcurrencyException(409, "Conflict");

    for (int i = 0; i <= retryStrategy.getRetryCount(); i++) {
      try {
        return updateInternal(aggregateId, update);
      } catch (ConcurrencyException concurrencyException) {
        lastException = concurrencyException;
        try {
          Thread.sleep(retryStrategy.getSleepMs());
        } catch (InterruptedException ie) {
          // ignore
        }
      }
    }

    throw lastException;

  }

  private int updateInternal(UUID aggregateId, AggregateUpdate<T> update) {
    assertValidUpdateConfig(update);

    if (update.stateCache().isPresent()) {
      StateCache<T> stateCache = update.stateCache().get();
      Optional<VersionedState<T>> cachedState = stateCache.get(aggregateId);

      final long currentVersion;

      final T currentState;
      if (cachedState.isPresent()) {
        VersionedState<T> versionedState = cachedState.get();
        currentVersion = versionedState.version();
        currentState = versionedState.state();
      } else {
        LoadAggregateResponse aggregateResponse = loadState(aggregateId, update.tenantId());
        currentVersion = aggregateResponse.aggregateVersion;
        currentState = stateBuilder.buildState(aggregateResponse.events);
      }

      try {
        List<Event<?>> events = update.apply(currentState);
        int eventStored = storeBatch(aggregateId, update.tenantId(), new EventBatch(events, currentVersion));
        if (eventStored > 0) {
          stateCache.put(aggregateId, new VersionedState<>(stateBuilder.buildState(currentState, events), currentVersion + 1));
        }
        return eventStored;
      } catch (ConcurrencyException e) {
        logger.log(INFO, format("Concurrency exception detected - invalidating cached entry with ID [%s]", aggregateId.toString()));
        stateCache.invalidate(aggregateId);
        throw e;
      }

    } else {
      LoadAggregateResponse aggregateResponse = loadState(aggregateId, update.tenantId());
      T state = stateBuilder.buildState(aggregateResponse.events);
      Long expectedVersion = update.useOptimisticConcurrencyOnUpdate() ? aggregateResponse.aggregateVersion : null;
      List<Event<?>> events = update.apply(state);
      return storeBatch(aggregateId, update.tenantId(), new EventBatch(events, expectedVersion));
    }

  }

  private void assertValidUpdateConfig(AggregateUpdate<T> update) {
    if (update.stateCache().isPresent() && !update.useOptimisticConcurrencyOnUpdate()) {
      throw new IllegalArgumentException("Cannot use stateCache with optimisticConcurrencyOnUpdate disabled");
    }
  }

  /**
   * Deletes all aggregate instances for current type or a single aggregate instance by its ID.
   *
   * @param delete Request
   * @return Confirmation for client to confirm.
   * @see AggregateDeleteConfirmation#confirm()
   */
  public AggregateDeleteConfirmation delete(AggregateDelete delete) {
    if (delete.aggregateId == null) {
      return getDeleteToken(getAggregateTypeUrl(), delete.tenantId);
    } else {
      return getDeleteToken(getAggregateUrl(delete.aggregateId), delete.tenantId);
    }
  }

  /**
   * Check if an aggregate exists.
   *
   * @param exists Request
   * @return True if aggregate with ID exists, false if not.
   */
  public boolean exists(AggregateExists exists) {
    try {
      HttpUrl url = getAggregateUrl(exists.aggregateId).build();
      if (exists.tenantId == null) {
        return client.head(url, Response::code) == 200;
      } else {
        return client.head(url, Response::code, exists.tenantId) == 200;
      }
    } catch (ApiException e) {
      if (e.statusCode() == 404) {
        return false;
      } else {
        throw e;
      }
    }
  }

  private AggregateDeleteConfirmation getDeleteToken(HttpUrl.Builder urlBuilder, UUID tenantId) {
    if (tenantId == null) {
      HttpUrl deleteAggregateUrl = extractDeleteToken(urlBuilder, client.delete(urlBuilder.build(), Map.class));
      return new AggregateDeleteConfirmation(client, deleteAggregateUrl);
    } else {
      HttpUrl deleteAggregateUrl = extractDeleteToken(urlBuilder, client.delete(urlBuilder.build(), Map.class, tenantId));
      return new AggregateDeleteConfirmation(client, deleteAggregateUrl, tenantId);
    }
  }

  private HttpUrl extractDeleteToken(HttpUrl.Builder urlBuilder, Map<String, String> deleteResponse) {
    return urlBuilder.addQueryParameter("deleteToken", deleteResponse.get("deleteToken")).build();
  }

  private LoadAggregateResponse loadState(UUID aggregateId, Optional<UUID> tenantId) {

    HttpUrl.Builder builder = getAggregateUrl(aggregateId).addQueryParameter("limit", String.valueOf(limit));

    int since = 0;
    LoadAggregateResponse response = new LoadAggregateResponse();

    if (tenantId.isPresent()) {
      do {
        HttpUrl url = builder.setQueryParameter("since", String.valueOf(since)).build();
        response.merge(client.get(url, LoadAggregateResponse.class, tenantId.get()));
        since += limit;
      } while (response.hasMore);

    } else {
      do {
        HttpUrl url = builder.setQueryParameter("since", String.valueOf(since)).build();
        response.merge(client.get(url, LoadAggregateResponse.class));
        since += limit;
      } while (response.hasMore);
    }

    return response;
  }

  private int storeBatch(UUID aggregateId, Optional<UUID> tenantId, EventBatch eventBatch) {
    if (eventBatch.events().isEmpty()) return 0;

    try {
      HttpUrl url = getAggregateUrl(aggregateId).addPathSegment("events").build();
      if (tenantId.isPresent()) {
        client.post(url, eventBatch, tenantId.get());
      } else {
        client.post(url, eventBatch);
      }
    } catch (ApiException e) {
      handleConcurrencyException(e);
    }
    return eventBatch.events().size();
  }

  private void handleConcurrencyException(ApiException e) {
    if (e.statusCode() == 409) {
      throw new ConcurrencyException(409, e.getMessage());
    } else {
      throw e;
    }
  }

  private HttpUrl.Builder getAggregateTypeUrl() {
    return apiRoot.newBuilder().addPathSegment("aggregates").addPathSegment(aggregateType);
  }

  private HttpUrl.Builder getAggregateUrl(UUID aggregateId) {
    return getAggregateTypeUrl().addPathSegment(aggregateId.toString());
  }

  public static class Builder<T> {

    private final ObjectMapper objectMapper = new ObjectMapper()
        .disable(FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(FAIL_ON_EMPTY_BEANS)
        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        .setSerializationInclusion(NON_NULL);

    private final HttpUrl apiRoot;
    private final OkHttpClient httpClient;
    private final StateBuilder<T> stateBuilder;

    private final String aggregateType;
    private final Map<String, Class> eventTypes = new HashMap<>();

    private RetryStrategy retryStrategy = RetryStrategy.DEFAULT;
    private UpdateStrategy updateStrategy = UpdateStrategy.DEFAULT;
    private int limit = 1000;

    Builder(String aggregateType, Class<T> stateClass, SerializedClientConfig config) {
      this.aggregateType = aggregateType;
      this.apiRoot = config.apiRoot();
      this.httpClient = config.httpClient();
      this.stateBuilder = stateBuilder(stateClass);
    }

    /**
     * Registers which handler should be invoked during aggregate load/hydration.
     */
    public <E> Builder<T> registerHandler(Class<E> eventClass, EventHandler<T, E> handler) {
      return registerHandler(eventClass.getSimpleName(), eventClass, handler);
    }

    /**
     * Registers which handler should be invoked during aggregate load/hydration.
     */
    public <E> Builder<T> registerHandler(String eventType, Class<E> eventClass, EventHandler<T, E> handler) {
      this.eventTypes.put(eventType, eventClass);
      stateBuilder.withHandler(eventClass, handler);
      return this;
    }

    public <E> Builder<T> withRetryStrategy(RetryStrategy retryStrategy) {
      this.retryStrategy = retryStrategy;
      return this;
    }

    public <E> Builder<T> withUpdateStrategy(UpdateStrategy updateStrategy) {
      this.updateStrategy = updateStrategy;
      return this;
    }

    /**
     * Limits the number of returned versions (event batches) on aggregate load (during update).
     *
     * @param limit The limit. Default is 1000 (maximum number according to the API).
     */
    public <E> Builder<T> withLimit(int limit) {
      this.limit = limit;
      return this;
    }

    /**
     * Allows object mapper customization.
     */
    public <E> Builder<T> configureObjectMapper(Consumer<ObjectMapper> consumer) {
      consumer.accept(objectMapper);
      return this;
    }

    public AggregateClient<T> build() {
      Validate.notNull(aggregateType, "'aggregateType' must be set");
      objectMapper.registerModule(EventDeserializer.module(eventTypes));
      stateBuilder.setFailOnMissingHandler(updateStrategy.failOnMissingHandler());
      stateBuilder.setIgnoredEventTypes(updateStrategy.ignoredEventTypes());
      return new AggregateClient<>(this);
    }
  }

  private static class LoadAggregateResponse {

    String aggregateId;
    String aggregateType;
    long aggregateVersion;
    List<Event<?>> events;
    boolean hasMore;

    public void merge(LoadAggregateResponse response) {
      this.aggregateId = response.aggregateId;
      this.aggregateType = response.aggregateType;
      this.aggregateVersion = response.aggregateVersion;
      if (events == null) {
        this.events = new ArrayList<>(response.events);
      } else {
        this.events.addAll(response.events);
      }
      this.hasMore = response.hasMore;
    }

  }

}
