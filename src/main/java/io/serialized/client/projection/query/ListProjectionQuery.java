package io.serialized.client.projection.query;

import okhttp3.HttpUrl;

import java.util.Optional;
import java.util.function.Function;

import static io.serialized.client.projection.ProjectionType.SINGLE;

public class ListProjectionQuery implements ProjectionQuery {

  private final Class responseClass;
  private final Function<HttpUrl, HttpUrl> urlBuilder;

  private ListProjectionQuery(Function<HttpUrl, HttpUrl> urlBuilder, Class responseClass) {
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
    private Integer limit;
    private String sort;

    public Builder(String projectionName) {
      this.projectionName = projectionName;
    }

    public Builder limit(int limit) {
      this.limit = limit;
      return this;
    }

    public Builder sortDescending(String field) {
      this.sort = "-" + field;
      return this;
    }

    public Builder sortAscending(String field) {
      this.sort = field;
      return this;
    }

    private HttpUrl urlBuilder(HttpUrl rootUrl) {
      HttpUrl.Builder projections = rootUrl.newBuilder()
          .addPathSegment("projections")
          .addPathSegment(SINGLE.name().toLowerCase())
          .addPathSegment(projectionName);

      Optional.ofNullable(limit).ifPresent(limit -> projections.addQueryParameter("limit", String.valueOf(limit)));
      Optional.ofNullable(sort).ifPresent(limit -> projections.addQueryParameter("sort", sort));

      return projections.build();
    }

    public ListProjectionQuery build(Class responseClass) {
      return new ListProjectionQuery(this::urlBuilder, responseClass);
    }

  }

}
