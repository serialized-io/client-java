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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static io.serialized.client.aggregate.StateBuilder.stateBuilder;

public class AggregateClient<T> {

  private final SerializedOkHttpClient client;
  private final HttpUrl apiRoot;
  private final StateBuilder<T> stateBuilder;
  private final String aggregateType;

  private AggregateClient(Builder<T> builder) {
    this.client = new SerializedOkHttpClient(builder.httpClient, builder.objectMapper);
    this.apiRoot = builder.apiRoot;
    this.aggregateType = builder.aggregateType;
    this.stateBuilder = builder.stateBuilder;
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
    return updateInternal(aggregateId, update);
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
      return extractDeleteToken(urlBuilder, client.delete(urlBuilder.build(), Map.class));
    } else {
      return extractDeleteToken(urlBuilder, client.delete(urlBuilder.build(), Map.class, tenantId));
    }
  }

  private AggregateDeleteConfirmation extractDeleteToken(HttpUrl.Builder urlBuilder, Map<String, String> deleteResponse) {
    String deleteToken = deleteResponse.get("deleteToken");
    HttpUrl deleteAggregateUrl = urlBuilder.addQueryParameter("deleteToken", deleteToken).build();
    return new AggregateDeleteConfirmation(client, deleteAggregateUrl);
  }

  private LoadAggregateResponse loadState(UUID aggregateId, Optional<UUID> tenantId) {
    HttpUrl url = getAggregateUrl(aggregateId).build();
    if (tenantId.isPresent()) {
      return client.get(url, LoadAggregateResponse.class, tenantId.get());
    } else {
      return client.get(url, LoadAggregateResponse.class);
    }
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

    Builder(String aggregateType, Class<T> stateClass, SerializedClientConfig config) {
      this.aggregateType = aggregateType;
      this.apiRoot = config.apiRoot();
      this.httpClient = config.httpClient();
      this.stateBuilder = stateBuilder(stateClass);
    }

    public <E> Builder<T> registerHandler(Class<E> eventClass, EventHandler<T, E> handler) {
      return registerHandler(eventClass.getSimpleName(), eventClass, handler);
    }

    public <E> Builder<T> registerHandler(String eventType, Class<E> eventClass, EventHandler<T, E> handler) {
      this.eventTypes.put(eventType, eventClass);
      stateBuilder.withHandler(eventClass, handler);
      return this;
    }

    public Builder configureObjectMapper(Consumer<ObjectMapper> consumer) {
      consumer.accept(objectMapper);
      return this;
    }

    public AggregateClient<T> build() {
      Validate.notNull(aggregateType, "'aggregateType' must be set");
      objectMapper.registerModule(EventDeserializer.module(eventTypes));
      return new AggregateClient<>(this);
    }
  }

  private static class LoadAggregateResponse {

    String aggregateId;
    String aggregateType;
    long aggregateVersion;
    List<Event<?>> events;

  }

}
