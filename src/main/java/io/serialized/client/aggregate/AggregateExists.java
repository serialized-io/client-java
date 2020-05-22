package io.serialized.client.aggregate;

import java.util.UUID;

public class AggregateExists {

  public final UUID tenantId;
  public final UUID aggregateId;

  private AggregateExists(Builder builder) {
    this.tenantId = builder.tenantId;
    this.aggregateId = builder.aggregateId;
  }

  public static Builder existsRequest() {
    return new Builder();
  }

  public static class Builder {

    private UUID aggregateId;
    private UUID tenantId;

    public Builder withAggregateId(UUID aggregateId) {
      this.aggregateId = aggregateId;
      return this;
    }

    public Builder withAggregateId(String aggregateId) {
      return this.withAggregateId(UUID.fromString(aggregateId));
    }

    public Builder withTenantId(UUID tenantId) {
      this.tenantId = tenantId;
      return this;
    }

    public Builder withTenantId(String tenantId) {
      return this.withTenantId(UUID.fromString(tenantId));
    }

    public AggregateExists build() {
      return new AggregateExists(this);
    }

  }

}
