package io.serialized.client.projection;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.SerializedOkHttpClient;
import io.serialized.client.projection.query.ListProjectionQuery;
import io.serialized.client.projection.query.ProjectionQuery;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import java.util.Map;

public class ProjectionApiClient {

  private final SerializedOkHttpClient client;
  private final ObjectMapper objectMapper;
  private final HttpUrl apiRoot;

  private ProjectionApiClient(Builder builder) {
    this.client = new SerializedOkHttpClient(builder.httpClient, builder.objectMapper);
    this.objectMapper = builder.objectMapper;
    this.apiRoot = builder.apiRoot;
  }

  public void createOrUpdate(ProjectionDefinition projectionDefinition) {
    HttpUrl url = apiRoot.newBuilder()
        .addPathSegment("projections")
        .addPathSegment("definitions")
        .addPathSegment(projectionDefinition.projectionName()).build();

    client.put(url, projectionDefinition);
  }

  public <T> ProjectionResponse<T> query(ProjectionQuery projectionQuery) {

    HttpUrl url = projectionQuery.constructUrl(apiRoot);

    JavaType javaType = projectionQuery.responseClass()
        .map(dataClass -> objectMapper.getTypeFactory().constructParametricType(ProjectionResponse.class, dataClass))
        .orElse(objectMapper.getTypeFactory().constructParametricType(ProjectionResponse.class, Map.class));

    return client.get(url, javaType);
  }

  public <T> ProjectionsResponse<T> list(ListProjectionQuery query) {

    HttpUrl url = query.constructUrl(apiRoot);

    JavaType javaType = query.responseClass()
        .map(dataClass -> objectMapper.getTypeFactory().constructParametricType(ProjectionsResponse.class, dataClass))
        .orElse(objectMapper.getTypeFactory().constructParametricType(ProjectionResponse.class, Map.class));

    return client.get(url, javaType);

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
