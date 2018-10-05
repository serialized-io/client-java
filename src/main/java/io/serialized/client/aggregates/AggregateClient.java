package io.serialized.client.aggregates;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.serialized.client.SerializedClientConfig;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static io.serialized.client.SerializedClientConfig.JSON_MEDIA_TYPE;
import static io.serialized.client.aggregates.EventBatch.newBatch;

public class AggregateClient<T extends State> {

  private final Class<T> stateClass;
  private final Map<String, EventHandler> handlers;
  private final String aggregateType;
  private final HttpUrl apiRoot;
  private final OkHttpClient httpClient;
  private final ObjectMapper objectMapper;

  private AggregateClient(Builder<T> builder) {
    this.aggregateType = builder.aggregateType;
    this.stateClass = builder.stateClass;
    this.handlers = builder.handlers;
    this.apiRoot = builder.apiRoot;
    this.httpClient = builder.httpClient;
    this.objectMapper = builder.objectMapper;
  }

  public T buildState(List<? extends Event> events) {
    try {
      AtomicReference<T> currentState = new AtomicReference<>(stateClass.newInstance());
      events.forEach(e -> {
            String simpleName = e.data().getClass().getSimpleName();
            EventHandler handler = handlers.get(simpleName);
            T handle = (T) handler.handle(currentState.get(), e);
            currentState.set(handle);
          }
      );
      return currentState.get();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException("Failed to build State", e);
    }
  }

  public static <T extends State> Builder<T> aggregateClient(String aggregateType, Class<T> stateClass, SerializedClientConfig config) {
    return new Builder<>(aggregateType, stateClass, config);
  }

  public T loadState(String aggregateId) {
    LoadAggregateResponse loadAggregateResponse = loadEvents(aggregateId);
    return buildState(loadAggregateResponse.events());
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

  private <T> T responseFor(Request request, Class<T> responseClass) throws IOException {
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

  public static class Builder<T extends State> {

    private final Class<T> stateClass;
    private final HttpUrl apiRoot;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public String aggregateType;
    private Map<String, EventHandler> handlers = new HashMap<>();
    private Map<String, Class> eventTypes = new HashMap<>();

    public Builder(String aggregateType, Class<T> stateClass, SerializedClientConfig config) {
      this.aggregateType = aggregateType;
      this.apiRoot = config.apiRoot();
      this.httpClient = config.httpClient();
      this.objectMapper = config.objectMapper();
      this.stateClass = stateClass;
    }

    public <E> Builder<T> registerHandler(Class<E> eventType, EventHandler<T, E> handler) {
      this.eventTypes.put(eventType.getSimpleName(), eventType);
      this.handlers.put(eventType.getSimpleName(), handler);
      return this;
    }

    public AggregateClient<T> build() {
      objectMapper.registerModule(EventDeserializer.module(eventTypes));
      return new AggregateClient<>(this);
    }
  }

}