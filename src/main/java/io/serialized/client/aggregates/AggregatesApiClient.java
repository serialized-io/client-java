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

public class AggregatesApiClient {

  private final OkHttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final HttpUrl apiRoot;

  private AggregatesApiClient(Builder builder) {
    this.httpClient = builder.httpClient;
    this.objectMapper = builder.objectMapper;
    this.apiRoot = builder.apiRoot;
  }

  public void storeEvent(String aggregateType, String aggregateId, Event event) {
    EventBatch eventBatch = newBatch(aggregateId).addEvent(event).build();
    storeEvents(aggregateType, eventBatch);
  }

  public void storeEvents(String aggregateType, EventBatch eventBatch) {

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

  public LoadAggregateResponse loadEvents(String aggregateType, String aggregateId) {
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

  public static Builder aggregatesClient(SerializedClientConfig config) {
    return new Builder(config);
  }

  public static class Builder {

    private final OkHttpClient httpClient;
    private ObjectMapper objectMapper;
    private final HttpUrl apiRoot;
    private final Map<String, Class> eventTypes = new HashMap<>();

    Builder(SerializedClientConfig config) {
      this.httpClient = config.httpClient();
      this.objectMapper = config.objectMapper();
      this.apiRoot = config.apiRoot();
    }

    public Builder registerEventType(Class eventClass) {
      return registerEventType(eventClass.getSimpleName(), eventClass);
    }

    public Builder registerEventType(String eventType, Class eventClass) {
      this.eventTypes.put(eventType, eventClass);
      return this;
    }

    public AggregatesApiClient build() {
      objectMapper.registerModule(EventDeserializer.module(eventTypes));
      return new AggregatesApiClient(this);
    }
  }
}
