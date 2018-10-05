package io.serialized.client.projection;

import okhttp3.HttpUrl;

import java.util.Optional;
import java.util.function.Function;

import static io.serialized.client.projection.ProjectionType.AGGREGATED;
import static io.serialized.client.projection.ProjectionType.SINGLE;

public class ProjectionQuery implements Query {

  private final Class responseClass;
  private final Function<HttpUrl, HttpUrl> urlBuilder;

  public ProjectionQuery(Function<HttpUrl, HttpUrl> urlBuilder, Class responseClass) {
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

  public static ListQueryBuilder list(String projectionName) {
    return new ListQueryBuilder(projectionName);
  }

  public static Builder singleProjection(String projectionName) {
    return new Builder(SINGLE, projectionName);
  }

  public static Builder aggregatedProjection(String projectionName) {
    return new Builder(AGGREGATED, projectionName);
  }

  public static class ListQueryBuilder {

    private final String projectionName;
    private Integer limit;
    private String sort;

    public ListQueryBuilder(String projectionName) {
      this.projectionName = projectionName;
    }

    public ListQueryBuilder limit(int limit) {
      this.limit = limit;
      return this;
    }

    public ListQueryBuilder sortDescending(String field) {
      this.sort = "-" + field;
      return this;
    }

    public ListQueryBuilder sortAscending(String field) {
      this.sort = field;
      return this;
    }

    private HttpUrl urlBuilder(HttpUrl rootUrl) {
      HttpUrl.Builder projections = rootUrl.newBuilder()
          .addPathSegment("projections")
          .addPathSegment(SINGLE.name())
          .addPathSegment(projectionName);

      Optional.ofNullable(limit).ifPresent(limit -> projections.addQueryParameter("limit", String.valueOf(limit)));
      Optional.ofNullable(sort).ifPresent(limit -> projections.addQueryParameter("sort", sort));

      return projections
          .build();
    }

    public ProjectionQuery build(Class responseClass) {
      return new ProjectionQuery(this::urlBuilder, responseClass);
    }

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

    public ProjectionQuery build(Class responseClass) {
      return new ProjectionQuery(this::urlBuilder, responseClass);
    }

  }

}
