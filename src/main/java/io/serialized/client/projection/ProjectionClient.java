package io.serialized.client.projection;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.SerializedOkHttpClient;
import io.serialized.client.projection.query.ProjectionQuery;
import io.serialized.client.projection.query.ProjectionsQuery;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;

public class ProjectionClient {

  private final SerializedOkHttpClient client;
  private final HttpUrl apiRoot;
  private final ObjectMapper objectMapper;

  private ProjectionClient(Builder builder) {
    this.client = new SerializedOkHttpClient(builder.httpClient, builder.objectMapper);
    this.apiRoot = builder.apiRoot;
    this.objectMapper = builder.objectMapper;
  }

  public static ProjectionClient.Builder projectionClient(SerializedClientConfig config) {
    return new ProjectionClient.Builder(config);
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
    String projectionName = projectionDefinition.projectionName();
    HttpUrl url = pathForDefinitions().addPathSegment(projectionName).build();
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

  public ProjectionDefinition getDefinition(String projectionName) {
    HttpUrl url = pathForDefinitions().addPathSegment(projectionName).build();
    return client.get(url, ProjectionDefinition.class);
  }

  public ProjectionDefinitions listDefinitions() {
    HttpUrl url = pathForDefinitions().build();
    return client.get(url, ProjectionDefinitions.class);
  }

  public void deleteDefinition(String projectionName) {
    HttpUrl url = pathForDefinitions().addPathSegment(projectionName).build();
    client.delete(url);
  }

  /**
   * This call deletes all existing projections and starts a rebuild from the beginning of the event history.
   * Keep in mind that this might take a while.
   */
  public void delete(ProjectionRequest request) {
    HttpUrl url = pathForProjections(request.projectionName, request.projectionType).build();
    if (request.tenantId().isPresent()) {
      client.delete(url, request.tenantId);
    } else {
      client.delete(url);
    }
  }

  public long count(ProjectionRequest request) {
    HttpUrl.Builder builder = pathForProjections(request.projectionName, request.projectionType).addPathSegment("_count");
    Optional.ofNullable(request.reference).ifPresent(reference -> builder.addQueryParameter("reference", request.reference));

    HttpUrl url = builder.build();

    if (request.tenantId().isPresent()) {
      return ((Number) client.get(url, Map.class, request.tenantId).get("count")).longValue();
    } else {
      return ((Number) client.get(url, Map.class).get("count")).longValue();
    }
  }

  public <T> ProjectionResponse<T> query(ProjectionQuery query) {
    JavaType javaType = query.responseClass()
        .map(dataClass -> objectMapper.getTypeFactory().constructParametricType(ProjectionResponse.class, dataClass))
        .orElse(objectMapper.getTypeFactory().constructParametricType(ProjectionResponse.class, Map.class));

    HttpUrl url = query.constructUrl(apiRoot);
    return getProjections(query, url, javaType);
  }

  public <T> ProjectionsResponse<T> query(ProjectionsQuery query) {
    JavaType javaType = query.responseClass()
        .map(dataClass -> objectMapper.getTypeFactory().constructParametricType(ProjectionsResponse.class, dataClass))
        .orElse(objectMapper.getTypeFactory().constructParametricType(ProjectionResponse.class, Map.class));

    if (query.isAutoPagination()) {

      AtomicReference<ProjectionsResponse<T>> currentPage = new AtomicReference<>();
      HttpUrl url = query.constructUrl(apiRoot);
      currentPage.set(getProjections(url, query, javaType));

      return new ProjectionsResponse<>(new ArrayList<ProjectionResponse<T>>(currentPage.get().projections()) {
        private boolean exhausted = false;

        @Override
        public void forEach(Consumer<? super ProjectionResponse<T>> action) {
          for (ProjectionResponse<T> item : this) {
            action.accept(item);
          }
        }

        @Override
        public Spliterator<ProjectionResponse<T>> spliterator() {
          return Spliterators.spliteratorUnknownSize(this.iterator(), 0);
        }

        @Override
        public Iterator<ProjectionResponse<T>> iterator() {
          if (exhausted) {
            throw new IllegalStateException("Iterator is already exhausted");
          }
          AtomicReference<Iterator<ProjectionResponse<T>>> delegateIterator = new AtomicReference<>(super.iterator());

          return new Iterator<ProjectionResponse<T>>() {
            @Override
            public boolean hasNext() {
              if (delegateIterator.get().hasNext()) {
                return true;
              } else {
                if (currentPage.get().hasMore()) {
                  HttpUrl url = query.constructUrl(apiRoot);
                  currentPage.set(getProjections(url, query, javaType));
                  Iterator<ProjectionResponse<T>> iterator = currentPage.get().iterator();
                  delegateIterator.set(iterator);
                  return iterator.hasNext();
                } else {
                  exhausted = true;
                  return false;
                }
              }
            }

            @Override
            public ProjectionResponse<T> next() {
              return delegateIterator.get().next();
            }
          };
        }
      });

    } else {
      HttpUrl url = query.constructUrl(apiRoot);
      return getProjections(url, query, javaType);
    }

  }

  private <T> ProjectionResponse<T> getProjections(ProjectionQuery query, HttpUrl url, JavaType javaType) {
    if (query.tenantId().isPresent()) {
      return client.get(url, javaType, query.tenantId().get());
    } else {
      return client.get(url, javaType);
    }
  }

  private <T> ProjectionsResponse<T> getProjections(HttpUrl url, ProjectionsQuery query, JavaType javaType) {
    if (query.tenantId().isPresent()) {
      return client.get(url, javaType, query.tenantId().get());
    } else {
      return client.get(url, javaType);
    }
  }

  private HttpUrl.Builder pathForDefinitions() {
    return apiRoot.newBuilder()
        .addPathSegment("projections")
        .addPathSegment("definitions");
  }

  private HttpUrl.Builder pathForProjections(String projectionName, ProjectionType type) {
    return apiRoot.newBuilder()
        .addPathSegment("projections")
        .addPathSegment(type.name().toLowerCase())
        .addPathSegment(projectionName);
  }

  public static class Builder {

    private final ObjectMapper objectMapper = new ObjectMapper()
        .disable(FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(FAIL_ON_EMPTY_BEANS)
        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        .setSerializationInclusion(NON_NULL);

    private final OkHttpClient httpClient;
    private final HttpUrl apiRoot;

    public Builder(SerializedClientConfig config) {
      this.httpClient = config.newHttpClient();
      this.apiRoot = config.apiRoot();
    }

    /**
     * Allows object mapper customization.
     */
    public Builder configureObjectMapper(Consumer<ObjectMapper> consumer) {
      consumer.accept(objectMapper);
      return this;
    }

    public ProjectionClient build() {
      return new ProjectionClient(this);
    }

  }

}
