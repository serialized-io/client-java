package io.serialized.client.projection.query;

import io.serialized.client.projection.ProjectionType;
import okhttp3.HttpUrl;

import java.util.Optional;
import java.util.function.Function;

import static io.serialized.client.projection.ProjectionType.AGGREGATED;
import static io.serialized.client.projection.ProjectionType.SINGLE;

/**
 * A query object
 */
public class AggregatedProjectionQuery implements Query {

  private final Class responseClass;
  private final Function<HttpUrl, HttpUrl> urlBuilder;

  public AggregatedProjectionQuery(Function<HttpUrl, HttpUrl> urlBuilder, Class responseClass) {
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

  public static Builder aggregatedProjection(String projectionName) {
    return new Builder(AGGREGATED, projectionName);
  }

  public static class Builder {

    private final ProjectionType projectionType;
    private final String projectionName;
    private String projectionId;

    public Builder(ProjectionType projectionType, String projectionName) {
      this.projectionType = projectionType;
      this.projectionName = projectionName;
    }

    public Builder id(String projectionId) {
      this.projectionId = projectionId;
      return this;
    }

    private HttpUrl urlBuilder(HttpUrl rootUrl) {
      if (SINGLE.equals(projectionType)) {
        return rootUrl.newBuilder()
            .addPathSegment("projections")
            .addPathSegment(projectionType.name().toLowerCase())
            .addPathSegment(projectionName)
            .addPathSegment(projectionId)
            .build();
      } else if (AGGREGATED.equals(projectionType)) {
        return rootUrl.newBuilder()
            .addPathSegment("projections")
            .addPathSegment(projectionType.name().toLowerCase())
            .addPathSegment(projectionName)
            .build();
      } else {
        throw new IllegalStateException("Invalid projectionType: " + projectionType);
      }
    }

    public AggregatedProjectionQuery build(Class responseClass) {
      return new AggregatedProjectionQuery(this::urlBuilder, responseClass);
    }

  }

}
