package io.serialized.client.reaction;

import java.util.Optional;
import java.util.UUID;

public class ExecuteReactionRequest {

  public final UUID tenantId;
  public final UUID reactionId;

  private ExecuteReactionRequest(Builder builder) {
    this.tenantId = builder.tenantId;
    this.reactionId = builder.reactionId;
  }

  public Optional<UUID> tenantId() {
    return Optional.ofNullable(this.tenantId);
  }

  public static class Builder {

    private UUID tenantId;
    private UUID reactionId;

    public Builder withTenantId(UUID tenantId) {
      this.tenantId = tenantId;
      return this;
    }

    public Builder withReactionId(UUID reactionId) {
      this.reactionId = reactionId;
      return this;
    }

    public ExecuteReactionRequest build() {
      return new ExecuteReactionRequest(this);
    }

  }

}
