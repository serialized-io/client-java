package io.serialized.client.feed;

import java.util.Optional;
import java.util.UUID;

public class GetSequenceNumberRequest {

  public final String feedName;
  public final UUID tenantId;

  private GetSequenceNumberRequest(Builder builder) {
    this.feedName = builder.feedName;
    this.tenantId = builder.tenantId;
  }

  public Optional<UUID> tenantId() {
    return Optional.ofNullable(this.tenantId);
  }

  public static class Builder {

    private String feedName = "_all";
    private UUID tenantId;

    public Builder withFeed(String feedName) {
      this.feedName = feedName;
      return this;
    }

    public Builder withTenantId(UUID tenantId) {
      this.tenantId = tenantId;
      return this;
    }

    public GetSequenceNumberRequest build() {
      return new GetSequenceNumberRequest(this);
    }

  }

}
