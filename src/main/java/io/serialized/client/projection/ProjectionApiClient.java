package io.serialized.client.projection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.serialized.client.SerializedClientConfig;
import okhttp3.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static io.serialized.client.SerializedClientConfig.JSON_MEDIA_TYPE;

public class ProjectionApiClient {

  private final OkHttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final HttpUrl apiRoot;

  private ProjectionApiClient(Builder builder) {
    this.httpClient = builder.httpClient;
    this.objectMapper = builder.objectMapper;
    this.apiRoot = builder.apiRoot;
  }

  public void createOrUpdate(ProjectionDefinition projectionDefinition) {
    Request request = new Request.Builder()
        .url(apiRoot.newBuilder().addPathSegment("projections").addPathSegment("definitions").addPathSegment(projectionDefinition.projectionName()).build())
        .put(RequestBody.create(JSON_MEDIA_TYPE, toJson(projectionDefinition)))
        .build();

    try (Response response = httpClient.newCall(request).execute()) {
      if (!response.isSuccessful()) throw new RuntimeException("Unexpected code " + response);
    } catch (IOException e) {
      throw new RuntimeException("Failed to save projection", e);
    }
  }

  public <T> ProjectionResponse<T> query(ProjectionQuery query) {

    Request request = new Request.Builder()
        .url(query.constructUrl(apiRoot))
        .get()
        .build();

    JavaType javaType = query.responseClass()
        .map(dataClass -> objectMapper.getTypeFactory().constructParametricType(ProjectionResponse.class, dataClass))
        .orElse(objectMapper.getTypeFactory().constructParametricType(ProjectionResponse.class, Map.class));

    return readResponse(request, javaType);
  }

  private String toJson(Object payload) {
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Failed to parse json from object", e);
    }
  }

  private <T> T readResponse(Request request, JavaType type) {
    try (Response response = httpClient.newCall(request).execute()) {
      if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
      InputStream src = response.body().byteStream();
      return objectMapper.readValue(src, type);
    } catch (IOException e) {
      throw new RuntimeException("Failed to read ", e);
    }
  }


  public static ProjectionApiClient.Builder projectionsClient(SerializedClientConfig config) {
    return new ProjectionApiClient.Builder(config);
  }

  public static class Builder {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final HttpUrl apiRoot;

    Builder(SerializedClientConfig config) {
      this.httpClient = config.httpClient();
      this.objectMapper = config.objectMapper();
      this.apiRoot = config.apiRoot();
    }

    public ProjectionApiClient build() {
      return new ProjectionApiClient(this);
    }
  }

}
