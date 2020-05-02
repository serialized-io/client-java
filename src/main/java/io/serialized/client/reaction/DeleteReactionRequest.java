package io.serialized.client.reaction;

import java.util.UUID;

public class DeleteReactionRequest {

  public final String type;
  public final UUID tenantId;
  public final UUID reactionId;

  private DeleteReactionRequest(Builder builder) {
    this.type = builder.type;
    this.tenantId = builder.tenantId;
    this.reactionId = builder.reactionId;
  }

  public boolean hasTenantId() {
    return tenantId != null;
  }

  public static class Builder {

    private final String type = "scheduled";
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
