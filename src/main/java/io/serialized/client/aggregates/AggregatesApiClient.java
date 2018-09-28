package io.serialized.client.aggregates;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.serialized.client.SerializedClientConfig;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static io.serialized.client.SerializedClientConfig.JSON_MEDIA_TYPE;

public class AggregatesApiClient {

  private final OkHttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final HttpUrl apiRoot;

  public AggregatesApiClient(Builder builder) {
    this.httpClient = builder.httpClient;
    this.objectMapper = builder.objectMapper;
    this.apiRoot = builder.apiRoot;
  }

  public void storeEvents(String aggregateType, EventBatch eventBatch) throws IOException {

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
      if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
    }
  }

  public LoadAggregateResponse loadAggregate(String aggregateType, String aggregateId) throws IOException {
    HttpUrl.Builder urlBuilder = apiRoot.newBuilder().addPathSegment("aggregates").addPathSegment(aggregateType).addPathSegment(aggregateId);

    Request request = new Request.Builder()
        .url(urlBuilder.build())
        .get()
        .build();

    return responseFor(request, LoadAggregateResponse.class);
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

  public static Builder aggregatesApiClient(SerializedClientConfig config) {
    return new Builder(config);
  }

  public static class Builder {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final HttpUrl apiRoot;
    private final Map<String, Class> eventTypes = new HashMap<>();

    public Builder(SerializedClientConfig config) {
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