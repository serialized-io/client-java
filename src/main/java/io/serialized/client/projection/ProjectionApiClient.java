package io.serialized.client.projection;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.serialized.client.SerializedClientConfig;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class ProjectionApiClient {

  private final OkHttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final HttpUrl apiRoot;

  public ProjectionApiClient(Builder builder) {
    this.httpClient = builder.httpClient;
    this.objectMapper = builder.objectMapper;
    this.apiRoot = builder.apiRoot;
  }

  public <T> ProjectionResponse<T> query(ProjectionQuery query) throws IOException {

    Request request = new Request.Builder()
        .url(query.constructUrl(apiRoot))
        .get()
        .build();

    JavaType javaType = query.responseClass()
        .map(dataClass -> objectMapper.getTypeFactory().constructParametricType(ProjectionResponse.class, dataClass))
        .orElse(objectMapper.getTypeFactory().constructParametricType(ProjectionResponse.class, Map.class));

    return readResponse(request, javaType);
  }

  private <T> T readResponse(Request request, JavaType type) throws IOException {
    try (Response response = httpClient.newCall(request).execute()) {
      if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
      InputStream src = response.body().byteStream();
      return objectMapper.readValue(src, type);
    }
  }


  public static ProjectionApiClient.Builder projectionsApiClient(SerializedClientConfig config) {
    return new ProjectionApiClient.Builder(config);
  }

  public static class Builder {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final HttpUrl apiRoot;

    public Builder(SerializedClientConfig config) {
      this.httpClient = config.httpClient();
      this.objectMapper = config.objectMapper();
      this.apiRoot = config.apiRoot();
    }

    public ProjectionApiClient build() {
      return new ProjectionApiClient(this);
    }
  }

}
