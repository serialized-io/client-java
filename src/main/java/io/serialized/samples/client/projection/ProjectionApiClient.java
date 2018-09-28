package io.serialized.samples.client.projection;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
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

  public ProjectionApiClient(OkHttpClient httpClient, ObjectMapper objectMapper, HttpUrl apiRoot) {
    this.httpClient = httpClient;
    this.objectMapper = objectMapper;
    this.apiRoot = apiRoot;
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


}
