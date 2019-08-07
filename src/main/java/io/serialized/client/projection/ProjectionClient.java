package io.serialized.client.projection;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.SerializedOkHttpClient;
import io.serialized.client.projection.query.ListProjectionQuery;
import io.serialized.client.projection.query.ProjectionQuery;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import java.io.IOException;
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

  public void createDefinition(ProjectionDefinition projectionDefinition) {
    HttpUrl url = pathForDefinitions().build();
    client.post(url, projectionDefinition);
  }

  /**
   * Creates a Projection definition from a JSON String value.
   *
   * @param jsonString a JSON String with a valid Projection definition
   * @throws IOException if the given String is not a valid Projection definition
   */
  public void createDefinition(String jsonString) throws IOException {
    ProjectionDefinition projectionDefinition = objectMapper.readValue(jsonString, ProjectionDefinition.class);
    createDefinition(projectionDefinition);
  }

  public void createOrUpdate(ProjectionDefinition projectionDefinition) {
    HttpUrl url = pathForDefinitions().addPathSegment(projectionDefinition.getProjectionName()).build();
    client.put(url, projectionDefinition);
  }

  /**
   * Creates/updates a Projection definition from a JSON String value.
   *
   * @param jsonString a JSON String with a valid Projection definition
   * @throws IOException if the given String is not a valid Projection definition
   */
  public void createOrUpdate(String jsonString) throws IOException {
    ProjectionDefinition projectionDefinition = objectMapper.readValue(jsonString, ProjectionDefinition.class);
    createOrUpdate(projectionDefinition);
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

    if (query.tenantId().isPresent()) {
      return client.get(url, javaType, query.tenantId().get());
    } else {
      return client.get(url, javaType);
    }
  }

  public <T> ProjectionsResponse<T> query(ListProjectionQuery query) {
    HttpUrl url = query.constructUrl(apiRoot);

    JavaType javaType = query.responseClass()
        .map(dataClass -> objectMapper.getTypeFactory().constructParametricType(ProjectionsResponse.class, dataClass))
        .orElse(objectMapper.getTypeFactory().constructParametricType(ProjectionResponse.class, Map.class));

    if (query.tenantId().isPresent()) {
      return client.get(url, javaType, query.tenantId().get());
    } else {
      return client.get(url, javaType);
    }
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
