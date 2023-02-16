package io.serialized.client.reaction;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class ListReactionsRequest {

  public final String status;
  public final UUID tenantId;
  public final Integer skip;
  public final Integer limit;
  public final String from;
  public final String to;
  public final UUID aggregateId;
  public final UUID eventId;

  private ListReactionsRequest(Builder builder) {
    this.status = builder.status;
    this.tenantId = builder.tenantId;
    this.skip = builder.skip;
    this.limit = builder.limit;
    this.from = builder.from;
    this.to = builder.to;
    this.aggregateId = builder.aggregateId;
    this.eventId = builder.eventId;
  }

  public Optional<UUID> tenantId() {
    return Optional.ofNullable(this.tenantId);
  }

  public static class Builder {

    private String status = "ALL";
    private Integer skip;
    private Integer limit;
    private UUID aggregateId;
    private UUID eventId;
    private String from;
    private String to;
    private UUID tenantId;

    public Builder withSkip(int skip) {
      this.skip = skip;
      return this;
    }

    public Builder withLimit(int limit) {
      this.limit = limit;
      return this;
    }

    public Builder withTenantId(UUID tenantId) {
      this.tenantId = tenantId;
      return this;
    }

    public Builder withStatus(Reaction.Status status) {
      this.status = status.name();
      return this;
    }

    public Builder withAggregateId(UUID aggregateId) {
      this.aggregateId = aggregateId;
      return this;
    }

    public Builder withEventId(UUID eventId) {
      this.eventId = eventId;
      return this;
    }

    /**
     * @param from filter 'triggerAt' from (inclusive).
     */
    public Builder withFrom(long from) {
      this.from = String.valueOf(from);
      return this;
    }

    /**
     * @param from filter 'triggerAt' from (inclusive).
     */
    public Builder withFrom(Date from) {
      this.from = String.valueOf(from.getTime());
      return this;
    }

    /**
     * @param to filter 'triggerAt' to (exclusive).
     */
    public Builder withTo(long to) {
      this.to = String.valueOf(to);
      return this;
    }

    /**
     * @param to filter 'triggerAt' to (exclusive).
     */
    public Builder withTo(Date to) {
      this.to = String.valueOf(to.getTime());
      return this;
    }

    public ListReactionsRequest build() {
      return new ListReactionsRequest(this);
    }

  }

}
