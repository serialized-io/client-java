package io.serialized.client.tenant;

import java.util.UUID;

public class Tenant {

  private String tenantId;
  private String tenantNumber;
  private long addedAt;
  private String reference;
  private boolean deleted;

  public String tenantId() {
    return tenantId;
  }

  public String tenantNumber() {
    return tenantNumber;
  }

  public long addedAt() {
    return addedAt;
  }

  public String reference() {
    return reference;
  }

  public boolean deleted() {
    return deleted;
  }

  public static TenantBuilder newTenant(UUID tenantId) {
    return new TenantBuilder(tenantId);
  }

  public static class TenantBuilder {
    private final UUID tenantId;

    private String reference;

    TenantBuilder(UUID tenantId) {
      this.tenantId = tenantId;
    }

    public TenantBuilder reference(String reference) {
      this.reference = reference;
      return this;
    }

    public Tenant build() {
      Tenant tenant = new Tenant();
      tenant.tenantId = tenantId.toString();
      tenant.reference = reference;
      return tenant;
    }

  }

}
