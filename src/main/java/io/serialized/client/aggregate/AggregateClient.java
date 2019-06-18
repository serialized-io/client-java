package io.serialized.client.aggregate;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.serialized.client.ApiException;
import io.serialized.client.ConcurrencyException;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.SerializedOkHttpClient;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.apache.commons.lang3.Validate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.serialized.client.aggregate.StateBuilder.stateBuilder;

public class AggregateClient<T> {

  private final SerializedOkHttpClient client;
  private final HttpUrl apiRoot;
  private final StateBuilder<T> stateBuilder;
  private final String aggregateType;
  /**
   * Enables optimistic concurrency for aggregate updates.
   */
  private final boolean useOptimisticConcurrencyOnUpdate;

  private AggregateClient(Builder<T> builder) {
    this.client = new SerializedOkHttpClient(builder.httpClient, builder.objectMapper);
    this.apiRoot = builder.apiRoot;
    this.aggregateType = builder.aggregateType;
    this.stateBuilder = builder.stateBuilder;
    this.useOptimisticConcurrencyOnUpdate = builder.useOptimisticConcurrencyOnUpdate;
  }

  public static <T> Builder<T> aggregateClient(String aggregateType, Class<T> stateClass, SerializedClientConfig config) {
    return new Builder<>(aggregateType, stateClass, config);
  }

  /**
   * Save events as a new aggregate.
   * <p>
   * The ID of the aggregate must be unique for the aggregate type.
   *
   * @param aggregateId The ID of the new aggregate.
   * @param events      List of events to save.
   * @throws ConcurrencyException if aggregate with the same ID already exists.
   */
  public void save(String aggregateId, List<Event> events) {
    save(UUID.fromString(aggregateId), events);
  }

  /**
   * Save events as a new aggregate.
   * <p>
   * The ID of the aggregate must be unique for the aggregate type.
   *
   * @param aggregateId The ID of the new aggregate.
   * @param events      List of events to save.
   * @throws ConcurrencyException if aggregate with the same ID already exists.
   */
  public void save(UUID aggregateId, List<Event> events) {
    storeBatch(aggregateId, new EventBatch(events, 0L));
  }

  /**
   * Unconditionally append events to an aggregate.
   *
   * @param aggregateId The ID of the aggregate.
   * @param events      List of events to save.
   */
  public void append(String aggregateId, List<Event> events) {
    append(UUID.fromString(aggregateId), events);
  }

  /**
   * Unconditionally append events to an aggregate.
   *
   * @param aggregateId The ID of the aggregate.
   * @param events      List of events to save.
   */
  public void append(UUID aggregateId, List<Event> events) {
    storeBatch(aggregateId, new EventBatch(events, null));
  }

  /**
   * Append events to an aggregate using optimistic concurrency.
   *
   * @param aggregateId     The ID of the aggregate.
   * @param events          List of events to save.
   * @param expectedVersion Expected existing aggregate version for the aggregate.
   */
  public void append(String aggregateId, List<Event> events, long expectedVersion) {
    append(UUID.fromString(aggregateId), events, expectedVersion);
  }

  /**
   * Append events to an aggregate using optimistic concurrency.
   *
   * @param aggregateId     The ID of the aggregate.
   * @param events          List of events to save.
   * @param expectedVersion Expected existing aggregate version for the aggregate.
   */
  public void append(UUID aggregateId, List<Event> events, long expectedVersion) {
    storeBatch(aggregateId, new EventBatch(events, expectedVersion));
  }

  /**
   * Update the aggregate.
   * <p>
   * The update will be performed using optimistic concurrency check depending on the
   * {@link #useOptimisticConcurrencyOnUpdate} configuration flag.
   *
   * @param aggregateId The ID of the aggregate.
   * @param update      Function that executes business logic and returns the resulting domain events.
   */
  public void update(String aggregateId, AggregateUpdate<T> update) {
    update(UUID.fromString(aggregateId), update);
  }

  /**
   * Update the aggregate.
   * <p>
   * The update will be performed using optimistic concurrency check depending on the
   * {@link #useOptimisticConcurrencyOnUpdate} configuration flag.
   *
   * @param aggregateId The ID of the aggregate.
   * @param update      Function that executes business logic and returns the resulting domain events.
   */
  public void update(UUID aggregateId, AggregateUpdate<T> update) {
    LoadAggregateResponse aggregateResponse = loadState(aggregateId);
    T state = stateBuilder.buildState(aggregateResponse.events);
    Long expectedVersion = useOptimisticConcurrencyOnUpdate ? aggregateResponse.aggregateVersion : null;
    List<Event> events = update.apply(state);
    storeBatch(aggregateId, new EventBatch(events, expectedVersion));
  }

  /**
   * Check if an aggregate exists.
   *
   * @return True if aggregate with ID exists, false if not.
   */
  public boolean exists(UUID aggregateId) {
    HttpUrl url = apiRoot.newBuilder()
        .addPathSegment("aggregates")
        .addPathSegment(aggregateType)
        .addPathSegment(aggregateId.toString()).build();

    try {
      return client.head(url, Response::code) == 200;
    } catch (ApiException e) {
      if (e.getStatusCode() == 404) {
        return false;
      } else {
        throw e;
      }
    }
  }

  private LoadAggregateResponse loadState(UUID aggregateId) {
    HttpUrl url = apiRoot.newBuilder()
        .addPathSegment("aggregates")
        .addPathSegment(aggregateType)
        .addPathSegment(aggregateId.toString()).build();

    return client.get(url, LoadAggregateResponse.class);
  }

  private void storeBatch(UUID aggregateId, EventBatch eventBatch) {
    if (eventBatch.getEvents().isEmpty()) return;

    HttpUrl url = apiRoot.newBuilder()
        .addPathSegment("aggregates")
        .addPathSegment(aggregateType)
        .addPathSegment(aggregateId.toString())
        .addPathSegment("events")
        .build();

    try {
      client.post(url, eventBatch);
    } catch (ApiException e) {
      if (e.getStatusCode() == 409) {
        throw new ConcurrencyException(409, e.getMessage());
      } else {
        throw e;
      }
    }
  }

  public static class Builder<T> {

    private final HttpUrl apiRoot;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final StateBuilder<T> stateBuilder;

    private final String aggregateType;
    private final Map<String, Class> eventTypes = new HashMap<>();

    private boolean useOptimisticConcurrencyOnUpdate = true;

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

    public Builder<T> useOptimisticConcurrencyOnUpdate(boolean useOptimisticConcurrencyOnUpdate) {
      this.useOptimisticConcurrencyOnUpdate = useOptimisticConcurrencyOnUpdate;
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
    List<Event> events;

  }

}