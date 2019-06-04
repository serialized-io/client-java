package io.serialized.client.projection.query;

import okhttp3.HttpUrl;
import org.apache.commons.lang3.Validate;

import java.util.Optional;
import java.util.function.Function;

import static io.serialized.client.projection.ProjectionType.SINGLE;

public class SingleProjectionQuery implements ProjectionQuery {

  private final Class responseClass;
  private final Function<HttpUrl, HttpUrl> urlBuilder;

  private SingleProjectionQuery(Function<HttpUrl, HttpUrl> urlBuilder, Class responseClass) {
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

  public static class Builder {

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
          .addPathSegment(SINGLE.name().toLowerCase())
          .addPathSegment(projectionName)
          .addPathSegment(projectionId)
          .build();
    }

    public SingleProjectionQuery build(Class responseClass) {
      Validate.notEmpty(projectionName, "'projectionName' must be set");
      Validate.notEmpty(projectionId, "'projectionId' must be set");
      return new SingleProjectionQuery(this::urlBuilder, responseClass);
    }

  }

}
