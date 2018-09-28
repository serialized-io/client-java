package io.serialized.samples.client.aggregates;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;

import static io.serialized.samples.client.SerializedApiClient.JSON_MEDIA_TYPE;

public class AggregatesApiClient {

  private final OkHttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final HttpUrl apiRoot;

  public AggregatesApiClient(OkHttpClient httpClient, ObjectMapper objectMapper, HttpUrl apiRoot) {
    this.httpClient = httpClient;
    this.objectMapper = objectMapper;
    this.apiRoot = apiRoot;
  }

  public void storeEvents(EventBatch eventBatch) throws IOException {
    HttpUrl.Builder urlBuilder = apiRoot.newBuilder().addPathSegment("aggregates").addPathSegment(eventBatch.aggregateType).addPathSegment("events");

    Request request = new Request.Builder()
        .url(urlBuilder.build())
        .post(RequestBody.create(JSON_MEDIA_TYPE, toJson(eventBatch)))
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
}
