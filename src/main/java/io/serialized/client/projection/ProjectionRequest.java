package io.serialized.client.projection;

import java.util.UUID;

public class ProjectionRequest {

  public final ProjectionType projectionType;
  public final String projectionName;
  public final UUID tenantId;

  private ProjectionRequest(Builder builder) {
    this.projectionType = builder.projectionType;
    this.projectionName = builder.projectionName;
    this.tenantId = builder.tenantId;
  }

  public boolean hasTenantId() {
    return tenantId != null;
  }

  public static class Builder {

    private final ProjectionType projectionType;
    private String projectionName;
    private UUID tenantId;

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

    public ProjectionRequest build() {
      return new ProjectionRequest(this);
    }

  }

}
