package io.serialized.client.aggregates;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.serialized.client.SerializedClientConfig;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static io.serialized.client.SerializedClientConfig.JSON_MEDIA_TYPE;
import static io.serialized.client.aggregates.EventBatch.newBatch;
import static io.serialized.client.aggregates.StateBuilder.stateBuilder;

public class AggregateClient<T> {

  private final StateBuilder<T> stateBuilder;
  private final String aggregateType;
  private final HttpUrl apiRoot;
  private final OkHttpClient httpClient;
  private final ObjectMapper objectMapper;

  private AggregateClient(Builder<T> builder) {
    this.aggregateType = builder.aggregateType;
    this.stateBuilder = builder.stateBuilder;
    this.apiRoot = builder.apiRoot;
    this.httpClient = builder.httpClient;
    this.objectMapper = builder.objectMapper;
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

    String content = toJson(eventBatch);

    Request request = new Request.Builder()
        .url(url)
        .post(RequestBody.create(JSON_MEDIA_TYPE, content))
        .build();

    try (Response response = httpClient.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        throw new RuntimeException("Failed to store events " + response);
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to store events " + e.getMessage());
    }
  }

  public LoadAggregateResponse loadEvents(String aggregateId) {
    HttpUrl.Builder urlBuilder = apiRoot.newBuilder().addPathSegment("aggregates").addPathSegment(aggregateType).addPathSegment(aggregateId);

    Request request = new Request.Builder()
        .url(urlBuilder.build())
        .get()
        .build();

    try {
      return responseFor(request, LoadAggregateResponse.class);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load aggregate");
    }
  }

  private <R> R responseFor(Request request, Class<R> responseClass) throws IOException {
    try (Response response = httpClient.newCall(request).execute()) {
      String responseContents = response.body().string();
      return objectMapper.readValue(responseContents, responseClass);
    }
  }

  private String toJson(Object payload) {
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to parse json request", e);
    }
  }

  public static class Builder<T> {

    private final HttpUrl apiRoot;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private StateBuilder<T> stateBuilder;

    public String aggregateType;
    private Map<String, Class> eventTypes = new HashMap<>();

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
      this.stateBuilder = stateBuilder.withHandler(eventClass, handler);
      return this;
    }

    public AggregateClient<T> build() {
      objectMapper.registerModule(EventDeserializer.module(eventTypes));
      return new AggregateClient<>(this);
    }
  }

}