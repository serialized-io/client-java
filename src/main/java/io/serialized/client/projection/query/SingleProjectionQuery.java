package io.serialized.client.projection.query;

import io.serialized.client.projection.ProjectionType;
import okhttp3.HttpUrl;

import java.util.Optional;
import java.util.function.Function;

import static io.serialized.client.projection.ProjectionType.SINGLE;

public class SingleProjectionQuery implements Query {

  private final Class responseClass;
  private final Function<HttpUrl, HttpUrl> urlBuilder;

  public SingleProjectionQuery(Function<HttpUrl, HttpUrl> urlBuilder, Class responseClass) {
    this.urlBuilder = urlBuilder;
    this.responseClass = responseClass;
  }

  @Override
  public HttpUrl constructUrl(HttpUrl rootUrl) {
    return urlBuilder.apply(rootUrl);
  }

  @Override
  public Optional<Class> responseClass() {
    return Optional.ofNullable(responseClass);
  }

  public static Builder singleProjection(String projectionName) {
    return new Builder(projectionName);
  }

  public static class Builder {

    private final ProjectionType projectionType = SINGLE;
    private final String projectionName;
    private String projectionId;

    public Builder(String projectionName) {
      this.projectionName = projectionName;
    }

    public Builder id(String projectionId) {
      this.projectionId = projectionId;
      return this;
    }

    private HttpUrl urlBuilder(HttpUrl rootUrl) {
      return rootUrl.newBuilder()
          .addPathSegment("projections")
          .addPathSegment(projectionType.name().toLowerCase())
          .addPathSegment(projectionName)
          .addPathSegment(projectionId)
          .build();
    }

    public SingleProjectionQuery build(Class responseClass) {
      return new SingleProjectionQuery(this::urlBuilder, responseClass);
    }

  }

}
