package io.serialized.client.projection.query;

import okhttp3.HttpUrl;
import org.apache.commons.lang3.Validate;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static io.serialized.client.projection.ProjectionType.SINGLE;

public class ListProjectionQuery implements ProjectionQuery {

  private final Class responseClass;
  private final Function<HttpUrl, HttpUrl> urlBuilder;
  private final UUID tenantId;

  private ListProjectionQuery(Function<HttpUrl, HttpUrl> urlBuilder, Class responseClass, UUID tenantId) {
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
    private Integer skip;
    private Integer limit;
    private String sort;
    private String reference;
    private String from;
    private String to;
    private UUID tenantId;
    private Iterable<String> ids;
    private SearchString searchString;

    public Builder(String projectionName) {
      this.projectionName = projectionName;
    }

    public Builder withSkip(int skip) {
      this.skip = skip;
      return this;
    }

    public Builder withLimit(int limit) {
      this.limit = limit;
      return this;
    }

    public Builder withSortDescending(String field) {
      this.sort = "-" + field;
      return this;
    }

    public Builder withSortAscending(String field) {
      this.sort = field;
      return this;
    }

    /**
     * Sort string.
     * Any combination of the following fields: projectionId, reference, createdAt, updatedAt.
     * Add '+' and '-' prefixes to indicate ascending/descending sort order.
     * Ascending order is default.
     *
     * @param string Sort string
     */
    public Builder withSort(String string) {
      this.sort = string;
      return this;
    }

    /**
     * @param reference String to filter result by
     */
    public Builder withReference(String reference) {
      this.reference = reference;
      return this;
    }

    /**
     * @param from filter reference from (inclusive).
     */
    public Builder withFrom(String from) {
      this.from = from;
      return this;
    }

    /**
     * @param from filter reference from (inclusive).
     */
    public Builder withFrom(long from) {
      this.from = String.valueOf(from);
      return this;
    }

    /**
     * @param from filter reference from (inclusive).
     */
    public Builder withFrom(Date from) {
      this.from = String.valueOf(from.getTime());
      return this;
    }

    /**
     * @param to filter reference to (inclusive).
     */
    public Builder withTo(String to) {
      this.to = to;
      return this;
    }

    /**
     * @param to filter reference to (inclusive).
     */
    public Builder withTo(long to) {
      this.to = String.valueOf(to);
      return this;
    }

    /**
     * @param to filter reference to (inclusive).
     */
    public Builder withTo(Date to) {
      this.to = String.valueOf(to.getTime());
      return this;
    }

    /**
     * If provided, filters on the projection id(s) to only the specified projections.
     * Provide multiple values to retrieve multiple projections in the response.
     *
     * @param ids Set of IDs of projections to fetch.
     */
    public Builder withIds(Iterable<String> ids) {
      this.ids = ids;
      return this;
    }

    public Builder withTenantId(UUID tenantId) {
      this.tenantId = tenantId;
      return this;
    }

    public Builder withSearchString(SearchString searchString) {
      this.searchString = searchString;
      return this;
    }

    private HttpUrl urlBuilder(HttpUrl rootUrl) {
      HttpUrl.Builder projections = rootUrl.newBuilder()
          .addPathSegment("projections")
          .addPathSegment(SINGLE.name().toLowerCase())
          .addPathSegment(projectionName);

      Optional.ofNullable(skip).ifPresent(skip -> projections.addQueryParameter("skip", String.valueOf(skip)));
      Optional.ofNullable(limit).ifPresent(limit -> projections.addQueryParameter("limit", String.valueOf(limit)));
      Optional.ofNullable(sort).ifPresent(sort -> projections.addQueryParameter("sort", sort));
      Optional.ofNullable(reference).ifPresent(reference -> projections.addQueryParameter("reference", reference));
      Optional.ofNullable(from).ifPresent(from -> projections.addQueryParameter("from", from));
      Optional.ofNullable(to).ifPresent(to -> projections.addQueryParameter("to", to));
      Optional.ofNullable(ids).ifPresent(ids -> ids.forEach(id -> projections.addQueryParameter("id", id)));
      Optional.ofNullable(searchString).ifPresent(searchString -> projections.addQueryParameter("search", searchString.string));

      return projections.build();
    }

    public ListProjectionQuery build(Class responseClass) {
      Validate.notEmpty(projectionName, "'projectionName' must be set");
      return new ListProjectionQuery(this::urlBuilder, responseClass, tenantId);
    }

  }

}
