package io.serialized.client.tenant;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.UUID;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

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

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
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
