package io.serialized.client.reaction;

import java.util.Optional;
import java.util.UUID;

public class DeleteReactionRequest {

  public final UUID tenantId;
  public final UUID reactionId;

  private DeleteReactionRequest(Builder builder) {
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

    public DeleteReactionRequest build() {
      return new DeleteReactionRequest(this);
    }

  }

}
