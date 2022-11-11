package io.serialized.client.reaction;

import java.util.Optional;
import java.util.UUID;

public class ListReactionsRequest {

  public final String status;
  public final UUID tenantId;
  public final Integer skip;
  public final Integer limit;

  private ListReactionsRequest(Builder builder) {
    this.status = builder.status;
    this.tenantId = builder.tenantId;
    this.skip = builder.skip;
    this.limit = builder.limit;
  }

  public Optional<UUID> tenantId() {
    return Optional.ofNullable(this.tenantId);
  }

  public static class Builder {

    private String status = "ALL";
    private Integer skip;
    private Integer limit;
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

    public ListReactionsRequest build() {
      return new ListReactionsRequest(this);
    }

  }

}
