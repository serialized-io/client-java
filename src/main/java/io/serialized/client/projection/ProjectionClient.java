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

public class ProjectionClient {

  private final SerializedOkHttpClient client;
  private final HttpUrl apiRoot;
  private final ObjectMapper objectMapper;

  private ProjectionClient(Builder builder) {
    this.client = new SerializedOkHttpClient(builder.httpClient, builder.objectMapper);
    this.apiRoot = builder.apiRoot;
    this.objectMapper = builder.objectMapper;
  }

  public void createOrUpdate(ProjectionDefinition projectionDefinition) {
    HttpUrl url = pathForDefinitions().addPathSegment(projectionDefinition.getProjectionName()).build();
    client.put(url, projectionDefinition);
  }

  public void deleteDefinition(String projectionName) {
    HttpUrl url = pathForDefinitions().addPathSegment(projectionName).build();
    client.delete(url);
  }

  public ProjectionDefinition getDefinition(String projectionName) {
    HttpUrl url = pathForDefinitions().addPathSegment(projectionName).build();
    return client.get(url, ProjectionDefinition.class);
  }

  public ProjectionDefinitions listDefinitions() {
    HttpUrl url = pathForDefinitions().build();
    return client.get(url, ProjectionDefinitions.class);
  }

  private HttpUrl.Builder pathForDefinitions() {
    return apiRoot.newBuilder()
        .addPathSegment("projections")
        .addPathSegment("definitions");
  }

  public <T> ProjectionResponse<T> query(ProjectionQuery query) {
    HttpUrl url = query.constructUrl(apiRoot);

    JavaType javaType = query.responseClass()
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

  public static ProjectionClient.Builder projectionClient(SerializedClientConfig config) {
    return new ProjectionClient.Builder(config);
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

    public ProjectionClient build() {
      return new ProjectionClient(this);
    }
  }

}
