package io.serialized.client.aggregate;

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

import static io.serialized.client.aggregate.StateBuilder.stateBuilder;

public class AggregateClient<T> {

  private final SerializedOkHttpClient client;
  private final HttpUrl apiRoot;
  private final StateBuilder<T> stateBuilder;
  private final String aggregateType;
  private final StateCache<T> stateCache;

  private AggregateClient(Builder<T> builder) {
    this.client = new SerializedOkHttpClient(builder.httpClient, builder.objectMapper);
    this.apiRoot = builder.apiRoot;
    this.aggregateType = builder.aggregateType;
    this.stateBuilder = builder.stateBuilder;
    this.stateCache = builder.stateCache;
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
   * @param tenantId    The ID of the tenant.
   * @param update      Function that executes business logic and returns the resulting domain events.
   * @return Number of events stored in batch
   */
  public int update(String aggregateId, String tenantId, AggregateUpdate<T> update) {
    return update(UUID.fromString(aggregateId), UUID.fromString(tenantId), update);
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
    return updateInternal(aggregateId, update, null);
  }

  /**
   * Update the aggregate.
   * <p>
   * The update will be performed using optimistic concurrency check depending on the
   * {@link AggregateUpdate#useOptimisticConcurrencyOnUpdate} configuration flag.
   *
   * @param aggregateId The ID of the aggregate.
   * @param tenantId    The ID of the tenant.
   * @param update      Function that executes business logic and returns the resulting domain events.
   * @return Number of events stored in batch
   */
  public int update(UUID aggregateId, UUID tenantId, AggregateUpdate<T> update) {
    return updateInternal(aggregateId, update, tenantId);
  }

  private int updateInternal(UUID aggregateId, AggregateUpdate<T> update, UUID tenantId) {
    assertValidUpdateConfig(update);

    if (update.useStateCache()) {
      Optional<VersionedState<T>> cachedState = Optional.ofNullable(tenantId)
          .map(id -> stateCache.get(aggregateId, tenantId))
          .orElseGet(() -> stateCache.get(aggregateId));

      final List<Event<?>> events;
      final long currentVersion;

      if (cachedState.isPresent()) {
        VersionedState<T> versionedState = cachedState.get();
        currentVersion = versionedState.version();
        events = update.apply(versionedState.state());
      } else {
        LoadAggregateResponse aggregateResponse = loadState(aggregateId, tenantId);
        currentVersion = aggregateResponse.aggregateVersion;
        events = update.apply(stateBuilder.buildState(aggregateResponse.events));
      }

      int eventStored = storeBatch(aggregateId, tenantId, new EventBatch(events, currentVersion));
      if (eventStored > 0) {
        if (tenantId == null) {
          stateCache.put(aggregateId, new VersionedState<>(stateBuilder.buildState(events), currentVersion + 1));
        } else {
          stateCache.put(aggregateId, tenantId, new VersionedState<>(stateBuilder.buildState(events), currentVersion + 1));
        }
      }
      return eventStored;

    } else {
      LoadAggregateResponse aggregateResponse = loadState(aggregateId, tenantId);
      T state = stateBuilder.buildState(aggregateResponse.events);
      Long expectedVersion = update.useOptimisticConcurrencyOnUpdate() ? aggregateResponse.aggregateVersion : null;
      List<Event<?>> events = update.apply(state);
      return storeBatch(aggregateId, tenantId, new EventBatch(events, expectedVersion));
    }

  }

  private void assertValidUpdateConfig(AggregateUpdate<T> update) {
    if (update.useStateCache() && !update.useOptimisticConcurrencyOnUpdate()) {
      throw new IllegalArgumentException("Cannot use stateCache with optimisticConcurrencyOnUpdate disabled");
    }
  }

  public AggregateDelete<T> deleteByType() {
    return getDeleteToken(getAggregateTypeUrl(), null);
  }

  public AggregateDelete<T> deleteByType(UUID tenantId) {
    return getDeleteToken(getAggregateTypeUrl(), tenantId);
  }

  public AggregateDelete<T> deleteById(UUID aggregateId) {
    return getDeleteToken(getAggregateUrl(aggregateId), null);
  }

  public AggregateDelete<T> deleteById(UUID aggregateId, UUID tenantId) {
    return getDeleteToken(getAggregateUrl(aggregateId), tenantId);
  }

  /**
   * Check if an aggregate exists.
   *
   * @param aggregateId ID of aggregate to check.
   * @return True if aggregate with ID exists, false if not.
   */
  public boolean exists(UUID aggregateId) {
    try {
      HttpUrl url = getAggregateUrl(aggregateId).build();
      return client.head(url, Response::code) == 200;
    } catch (ApiException e) {
      if (e.statusCode() == 404) {
        return false;
      } else {
        throw e;
      }
    }
  }

  /**
   * Check if an aggregate exists for the given tenant.
   *
   * @param aggregateId ID of aggregate to check.
   * @param tenantId    ID of tenant.
   * @return True if aggregate with ID exists, false if not.
   */
  public boolean exists(UUID aggregateId, UUID tenantId) {
    try {
      HttpUrl url = getAggregateUrl(aggregateId).build();
      return client.head(url, Response::code, tenantId) == 200;
    } catch (ApiException e) {
      if (e.statusCode() == 404) {
        return false;
      } else {
        throw e;
      }
    }
  }

  private AggregateDelete<T> getDeleteToken(HttpUrl.Builder urlBuilder, UUID tenantId) {
    if (tenantId == null) {
      return extractDeleteToken(urlBuilder, client.delete(urlBuilder.build(), Map.class));
    } else {
      return extractDeleteToken(urlBuilder, client.delete(urlBuilder.build(), Map.class, tenantId));
    }
  }

  private AggregateDelete<T> extractDeleteToken(HttpUrl.Builder urlBuilder, Map<String, String> deleteResponse) {
    String deleteToken = deleteResponse.get("deleteToken");
    HttpUrl deleteAggregateUrl = urlBuilder.addQueryParameter("deleteToken", deleteToken).build();
    return new AggregateDelete<>(client, deleteAggregateUrl);
  }

  private LoadAggregateResponse loadState(UUID aggregateId, UUID tenantId) {
    HttpUrl url = getAggregateUrl(aggregateId).build();
    if (tenantId == null) {
      return client.get(url, LoadAggregateResponse.class);
    } else {
      return client.get(url, LoadAggregateResponse.class, tenantId);
    }
  }

  private int storeBatch(UUID aggregateId, UUID tenantId, EventBatch eventBatch) {
    if (eventBatch.events().isEmpty()) return 0;

    try {
      HttpUrl url = getAggregateUrl(aggregateId).addPathSegment("events").build();
      if (tenantId == null) {
        client.post(url, eventBatch);
      } else {
        client.post(url, eventBatch, tenantId);
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

    private final HttpUrl apiRoot;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final StateBuilder<T> stateBuilder;

    private final String aggregateType;
    private final Map<String, Class> eventTypes = new HashMap<>();

    private StateCache<T> stateCache = new StateCache<T>() {
    };

    Builder(String aggregateType, Class<T> stateClass, SerializedClientConfig config) {
      this.aggregateType = aggregateType;
      this.apiRoot = config.apiRoot();
      this.httpClient = config.httpClient();
      this.objectMapper = config.objectMapper();
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

    public <E> Builder<T> withStateCache(StateCache<T> stateCache) {
      this.stateCache = stateCache;
      return this;
    }

    public AggregateClient<T> build() {
      Validate.notNull(aggregateType, "'aggregateType' must be set");
      Validate.notNull(stateCache, "'stateCache' cannot be null");
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
