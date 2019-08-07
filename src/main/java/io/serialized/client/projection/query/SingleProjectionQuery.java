package io.serialized.client.projection.query;

import okhttp3.HttpUrl;
import org.apache.commons.lang3.Validate;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static io.serialized.client.projection.ProjectionType.SINGLE;
import static java.time.temporal.ChronoUnit.SECONDS;

public class SingleProjectionQuery implements ProjectionQuery {

  private final Class responseClass;
  private final Function<HttpUrl, HttpUrl> urlBuilder;
  private final UUID tenantId;

  private SingleProjectionQuery(Function<HttpUrl, HttpUrl> urlBuilder, Class responseClass, UUID tenantId) {
    this.urlBuilder = urlBuilder;
    this.responseClass = responseClass;
    this.tenantId = tenantId;
  }

  @Override
  public HttpUrl constructUrl(HttpUrl rootUrl) {
    return urlBuilder.apply(rootUrl);
  }

  @Override
  public Optional<UUID> tenantId() {
    return Optional.ofNullable(this.tenantId);
  }

  @Override
  public Optional<Class> responseClass() {
    return Optional.ofNullable(responseClass);
  }

  public static class Builder {

    private final String projectionName;
    private String projectionId;
    private Duration duration;
    private UUID tenantId;

    public Builder(String projectionName) {
      this.projectionName = projectionName;
    }

    public Builder id(String projectionId) {
      this.projectionId = projectionId;
      return this;
    }

    public Builder awaitCreation(Duration duration) {
      this.duration = duration;
      return this;
    }

    public Builder withTenantId(UUID tenantId) {
      this.tenantId = tenantId;
      return this;
    }

    private HttpUrl urlBuilder(HttpUrl rootUrl) {
      HttpUrl.Builder builder = rootUrl.newBuilder()
          .addPathSegment("projections")
          .addPathSegment(SINGLE.name().toLowerCase())
          .addPathSegment(projectionName)
          .addPathSegment(projectionId);

      if (duration != null) {
        Validate.isTrue(duration.toMillis() > 0, "Duration must be positive");
        Validate.isTrue(duration.toMillis() < Duration.of(60, SECONDS).toMillis(), "Duration can be maximum 60s");
        builder.addQueryParameter("awaitCreation", String.valueOf(duration.toMillis()));
      }

      return builder.build();
    }

    public SingleProjectionQuery build(Class responseClass) {
      Validate.notEmpty(projectionName, "'projectionName' must be set");
      Validate.notEmpty(projectionId, "'projectionId' must be set");
      return new SingleProjectionQuery(this::urlBuilder, responseClass, tenantId);
    }

  }

}
