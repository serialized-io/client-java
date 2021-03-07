package io.serialized.client.reaction;

import java.util.Optional;
import java.util.UUID;

public class ReactionRequest {

  public final String type;
  public final UUID tenantId;

  private ReactionRequest(Builder builder) {
    this.type = builder.type;
    this.tenantId = builder.tenantId;
  }

  public Optional<UUID> tenantId() {
    return Optional.ofNullable(this.tenantId);
  }

  public static class Builder {

    private final String type;
    private UUID tenantId;

    public Builder(String type) {
      this.type = type;
    }

    public Builder withTenantId(UUID tenantId) {
      this.tenantId = tenantId;
      return this;
    }

    public ReactionRequest build() {
      return new ReactionRequest(this);
    }

  }

}
