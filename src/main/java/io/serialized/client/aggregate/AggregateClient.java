package io.serialized.client.aggregate;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.SerializedOkHttpClient;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
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
  private final boolean requireUniqueIdOnSave;
  private final boolean useOptimisticConcurrencyOnUpdate;

  private AggregateClient(Builder<T> builder) {
    this.client = new SerializedOkHttpClient(builder.httpClient, builder.objectMapper);
    this.apiRoot = builder.apiRoot;
    this.aggregateType = builder.aggregateType;
    this.stateBuilder = builder.stateBuilder;
    this.requireUniqueIdOnSave = builder.requireUniqueIdOnSave;
    this.useOptimisticConcurrencyOnUpdate = builder.useOptimisticConcurrencyOnUpdate;
  }

  public static <T> Builder<T> aggregateClient(String aggregateType, Class<T> stateClass, SerializedClientConfig config) {
    return new Builder<>(aggregateType, stateClass, config);
  }

  public void save(String aggregateId, List<Event> events) {
    save(UUID.fromString(aggregateId), events);
  }

  public void save(UUID aggregateId, List<Event> events) {
    storeBatch(new EventBatch(aggregateId, events, requireUniqueIdOnSave ? 0L : null));
  }

  public void update(String aggregateId, AggregateUpdate<T> update) {
    update(UUID.fromString(aggregateId), update);
  }

  public void update(UUID aggregateId, AggregateUpdate<T> update) {
    LoadAggregateResponse aggregateResponse = loadState(aggregateId.toString());
    T state = stateBuilder.buildState(aggregateResponse.events);
    Long expectedVersion = useOptimisticConcurrencyOnUpdate ? aggregateResponse.aggregateVersion : null;
    List<Event> events = update.apply(state);
    storeBatch(new EventBatch(aggregateId, events, expectedVersion));
  }

  private LoadAggregateResponse loadState(String aggregateId) {
    HttpUrl url = apiRoot.newBuilder()
        .addPathSegment("aggregates")
        .addPathSegment(aggregateType)
        .addPathSegment(aggregateId).build();

    return client.get(url, LoadAggregateResponse.class);
  }

  private void storeBatch(EventBatch eventBatch) {
    if (eventBatch.getEvents().isEmpty()) return;

    HttpUrl url = apiRoot.newBuilder()
        .addPathSegment("aggregates")
        .addPathSegment(aggregateType)
        .addPathSegment("events")
        .build();

    client.post(url, eventBatch);
  }

  public static class Builder<T> {

    private final HttpUrl apiRoot;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final StateBuilder<T> stateBuilder;

    private final String aggregateType;
    private final Map<String, Class> eventTypes = new HashMap<>();

    private boolean requireUniqueIdOnSave = true;
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

    public Builder<T> requireUniqueIdOnSave(boolean requireUniqueIdOnSave) {
      this.requireUniqueIdOnSave = requireUniqueIdOnSave;
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