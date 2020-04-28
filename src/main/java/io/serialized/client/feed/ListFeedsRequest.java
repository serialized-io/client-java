package io.serialized.client.feed;

import java.util.UUID;

public class ListFeedsRequest {

  public final UUID tenantId;

  private ListFeedsRequest(Builder builder) {
    this.tenantId = builder.tenantId;
  }

  public boolean hasTenantId() {
    return tenantId != null;
  }

  public static class Builder {

    private UUID tenantId;

    public Builder withTenantId(UUID tenantId) {
      this.tenantId = tenantId;
      return this;
    }

    public ListFeedsRequest build() {
      return new ListFeedsRequest(this);
    }

  }

}
