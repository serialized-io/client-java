package io.serialized.client.projection.query;

import okhttp3.HttpUrl;
import org.apache.commons.lang3.Validate;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static io.serialized.client.projection.ProjectionType.SINGLE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class ListProjectionsQuery implements ProjectionsQuery {

  private final Class responseClass;
  private final String projectionName;
  private final int skip;
  private final int limit;
  private final String sort;
  private final String reference;
  private final String from;
  private final String to;
  private final Iterable<String> ids;
  private final SearchString searchString;
  private final UUID tenantId;
  private final boolean autoPagination;
  private int position = -1;

  private ListProjectionsQuery(Builder builder, Class responseClass) {
    this.projectionName = builder.projectionName;
    this.skip = builder.skip;
    this.limit = builder.limit;
    this.sort = builder.sort;
    this.reference = builder.reference;
    this.from = builder.from;
    this.to = builder.to;
    this.ids = builder.ids;
    this.searchString = builder.searchString;
    this.tenantId = builder.tenantId;
    this.autoPagination = builder.autoPagination;
    this.responseClass = responseClass;
  }

  @Override
  public HttpUrl constructUrl(HttpUrl rootUrl) {
    return urlBuilder(rootUrl);
  }

  @Override
  public Optional<UUID> tenantId() {
    return Optional.ofNullable(this.tenantId);
  }

  @Override
  public Optional<Class> responseClass() {
    return Optional.ofNullable(responseClass);
  }

  @Override
  public boolean isAutoPagination() {
    return autoPagination;
  }

  private int calculatePosition() {
    if (position == -1) {
      position = skip;
    } else {
      position += limit;
    }
    return position;
  }

  private HttpUrl urlBuilder(HttpUrl rootUrl) {
    HttpUrl.Builder projections = rootUrl.newBuilder()
        .addPathSegment("projections")
        .addPathSegment(SINGLE.name().toLowerCase())
        .addPathSegment(projectionName);

    if (autoPagination) {
      projections.addQueryParameter("skip", String.valueOf(calculatePosition()));
    } else {
      projections.addQueryParameter("skip", String.valueOf(skip));
    }

    projections.addQueryParameter("limit", String.valueOf(limit));
    Optional.ofNullable(sort).ifPresent(sort -> projections.addQueryParameter("sort", sort));
    Optional.ofNullable(reference).ifPresent(reference -> projections.addQueryParameter("reference", reference));
    Optional.ofNullable(from).ifPresent(from -> projections.addQueryParameter("from", from));
    Optional.ofNullable(to).ifPresent(to -> projections.addQueryParameter("to", to));
    Optional.ofNullable(ids).ifPresent(ids -> ids.forEach(id -> projections.addQueryParameter("id", id)));
    Optional.ofNullable(searchString).ifPresent(searchString -> projections.addQueryParameter("search", searchString.string));

    return projections.build();
  }

  public static class Builder {

    private final String projectionName;
    private int skip = 0;
    private int limit = 100;
    private String sort;
    private String reference;
    private String from;
    private String to;
    private UUID tenantId;
    private Iterable<String> ids;
    private SearchString searchString;
    private boolean autoPagination;

    public Builder(String projectionName) {
      this.projectionName = projectionName;
    }

    public Builder withAutoPagination(boolean autoPagination) {
      this.autoPagination = autoPagination;
      return this;
    }

    public Builder withSkip(int skip) {
      Validate.isTrue(skip >= 0, "'skip' cannot be negative");
      this.skip = skip;
      return this;
    }

    /**
     * @param limit Limit, or page size if auto-pagination is enabled.
     */
    public Builder withLimit(int limit) {
      Validate.isTrue(limit > 0, "'limit' must be positive");
      this.limit = limit;
      return this;
    }

    public Builder withSortDescending(String field) {
      Validate.isTrue(isNotBlank(field));
      this.sort = "-" + field;
      return this;
    }

    public Builder withSortAscending(String field) {
      Validate.isTrue(isNotBlank(field));
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
      Validate.isTrue(isNotBlank(string));
      this.sort = string;
      return this;
    }

    /**
     * @param reference String to filter result by
     */
    public Builder withReference(String reference) {
      Validate.isTrue(isNotBlank(reference));
      this.reference = reference;
      return this;
    }

    /**
     * @param from filter reference from (inclusive).
     */
    public Builder withFrom(String from) {
      Validate.isTrue(isNotBlank(from));
      this.from = from;
      return this;
    }

    /**
     * @param from filter reference from (inclusive).
     */
    public Builder withFrom(long from) {
      Validate.isTrue(from >= 0, "'from' cannot be negative");
      this.from = String.valueOf(from);
      return this;
    }

    /**
     * @param from filter reference from (inclusive).
     */
    public Builder withFrom(Date from) {
      Validate.isTrue(from != null);
      this.from = String.valueOf(from.getTime());
      return this;
    }

    /**
     * @param to filter reference to (inclusive).
     */
    public Builder withTo(String to) {
      Validate.isTrue(isNotBlank(to));
      this.to = to;
      return this;
    }

    /**
     * @param to filter reference to (inclusive).
     */
    public Builder withTo(long to) {
      Validate.isTrue(to > 0, "'to' must be positive");
      this.to = String.valueOf(to);
      return this;
    }

    /**
     * @param to filter reference to (inclusive).
     */
    public Builder withTo(Date to) {
      Validate.isTrue(to != null);
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
      Validate.isTrue(ids != null);
      this.ids = ids;
      return this;
    }

    public Builder withTenantId(UUID tenantId) {
      Validate.isTrue(tenantId != null);
      this.tenantId = tenantId;
      return this;
    }

    public Builder withSearchString(SearchString searchString) {
      Validate.isTrue(searchString != null);
      this.searchString = searchString;
      return this;
    }

    public ListProjectionsQuery build(Class responseClass) {
      Validate.notEmpty(projectionName, "'projectionName' must be set");
      return new ListProjectionsQuery(this, responseClass);
    }

  }

}
