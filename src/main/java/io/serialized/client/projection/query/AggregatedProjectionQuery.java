package io.serialized.client.projection.query;

import okhttp3.HttpUrl;
import org.apache.commons.lang3.Validate;

import java.util.Optional;
import java.util.function.Function;

import static io.serialized.client.projection.ProjectionType.AGGREGATED;

/**
 * A query object
 */
public class AggregatedProjectionQuery implements ProjectionQuery {

  private final Class responseClass;
  private final Function<HttpUrl, HttpUrl> urlBuilder;

  private AggregatedProjectionQuery(Function<HttpUrl, HttpUrl> urlBuilder, Class responseClass) {
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

    public Builder(String projectionName) {
      this.projectionName = projectionName;
    }

    private HttpUrl urlBuilder(HttpUrl rootUrl) {
      return rootUrl.newBuilder()
          .addPathSegment("projections")
          .addPathSegment(AGGREGATED.name().toLowerCase())
          .addPathSegment(projectionName)
          .build();
    }

    public AggregatedProjectionQuery build(Class responseClass) {
      Validate.notEmpty(projectionName, "'projectionName' must be set");
      return new AggregatedProjectionQuery(this::urlBuilder, responseClass);
    }

  }

}
