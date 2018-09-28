package io.serialized.client.projection;

import okhttp3.HttpUrl;

import java.util.Optional;

import static io.serialized.client.projection.ProjectionType.AGGREGATED;
import static io.serialized.client.projection.ProjectionType.SINGLE;

public class ProjectionQuery {

  private final ProjectionType projectionType;
  private final String projectionName;
  private final String projectionId;
  private final Class responseClass;

  private ProjectionQuery(Builder builder) {
    this.projectionType = builder.projectionType;
    this.projectionName = builder.projectionName;
    this.projectionId = builder.projectionId;
    this.responseClass = builder.responseClass;
  }

  HttpUrl constructUrl(HttpUrl rootUrl) {
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

  Optional<Class> responseClass() {
    return Optional.ofNullable(responseClass);
  }

  public static Builder singleProjection(String projectionName) {
    return new Builder(SINGLE, projectionName);
  }

  public static Builder aggregatedProjection(String projectionName) {
    return new Builder(AGGREGATED, projectionName);
  }

  public static class Builder {

    private final ProjectionType projectionType;
    private final String projectionName;
    private String projectionId;
    private Class responseClass;

    public Builder(ProjectionType projectionType, String projectionName) {
      this.projectionType = projectionType;
      this.projectionName = projectionName;
    }

    public Builder id(String projectionId) {
      this.projectionId = projectionId;
      return this;
    }

    public Builder as(Class responseClass) {
      this.responseClass = responseClass;
      return this;
    }


    public ProjectionQuery build() {
      return new ProjectionQuery(this);
    }

  }

}
