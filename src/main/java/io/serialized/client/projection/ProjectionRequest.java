package io.serialized.client.projection;

import org.apache.commons.lang3.Validate;

import java.util.Optional;
import java.util.UUID;

public class ProjectionRequest {

  public final ProjectionType projectionType;
  public final String projectionName;
  public final UUID tenantId;
  public final String reference;

  private ProjectionRequest(Builder builder) {
    this.projectionType = builder.projectionType;
    this.projectionName = builder.projectionName;
    this.tenantId = builder.tenantId;
    this.reference = builder.reference;
  }

  public Optional<UUID> tenantId() {
    return Optional.ofNullable(this.tenantId);
  }

  public static class Builder {

    private final ProjectionType projectionType;
    private String projectionName;
    private UUID tenantId;
    private String reference;

    public Builder(ProjectionType projectionType) {
      this.projectionType = projectionType;
    }

    public Builder withProjectionName(String projectionName) {
      this.projectionName = projectionName;
      return this;
    }

    public Builder withTenantId(UUID tenantId) {
      this.tenantId = tenantId;
      return this;
    }

    public Builder withReference(String reference) {
      this.reference = reference;
      return this;
    }

    public ProjectionRequest build() {
      Validate.notEmpty(projectionName, "'projectionName' must be set");
      return new ProjectionRequest(this);
    }

  }

}
