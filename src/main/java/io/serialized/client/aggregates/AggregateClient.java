package io.serialized.client.aggregates;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.SerializedOkHttpClient;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import java.util.HashMap;
import java.util.Map;

import static io.serialized.client.aggregates.EventBatch.newBatch;
import static io.serialized.client.aggregates.StateBuilder.stateBuilder;

public class AggregateClient<T> {

  private final SerializedOkHttpClient client;
  private final StateBuilder<T> stateBuilder;
  private final String aggregateType;
  private final HttpUrl apiRoot;

  private AggregateClient(Builder<T> builder) {
    this.aggregateType = builder.aggregateType;
    this.stateBuilder = builder.stateBuilder;
    this.apiRoot = builder.apiRoot;
    this.client = new SerializedOkHttpClient(builder.httpClient, builder.objectMapper);
  }

  public static <T> Builder<T> aggregateClient(String aggregateType, Class<T> stateClass, SerializedClientConfig config) {
    return new Builder<>(aggregateType, stateClass, config);
  }

  public State<T> loadState(String aggregateId) {
    LoadAggregateResponse loadAggregateResponse = loadEvents(aggregateId);
    return stateBuilder.buildState(loadAggregateResponse.events(), loadAggregateResponse.aggregateVersion());
  }

  public void storeEvent(String aggregateId, Event event) {
    EventBatch eventBatch = newBatch(aggregateId).addEvent(event).build();
    storeEvents(eventBatch);
  }

  public void storeEvents(EventBatch eventBatch) {

    HttpUrl url = apiRoot.newBuilder()
        .addPathSegment("aggregates")
        .addPathSegment(aggregateType)
        .addPathSegment("events")
        .build();

    client.post(url, eventBatch);
  }

  public LoadAggregateResponse loadEvents(String aggregateId) {
    HttpUrl url = apiRoot.newBuilder()
        .addPathSegment("aggregates")
        .addPathSegment(aggregateType)
        .addPathSegment(aggregateId).build();

    return client.get(url, LoadAggregateResponse.class);

  }

  public static class Builder<T> {

    private final HttpUrl apiRoot;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final StateBuilder<T> stateBuilder;

    private final String aggregateType;
    private final Map<String, Class> eventTypes = new HashMap<>();

    public Builder(String aggregateType, Class<T> stateClass, SerializedClientConfig config) {
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

    public AggregateClient<T> build() {
      objectMapper.registerModule(EventDeserializer.module(eventTypes));
      return new AggregateClient<>(this);
    }
  }

}