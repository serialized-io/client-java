package io.serialized.client.tenant;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

public class TenantsResponse {

  private List<Tenant> tenants;

  public TenantsResponse() {
  }

  public TenantsResponse(List<Tenant> tenants) {
    this.tenants = tenants;
  }

  public List<Tenant> tenants() {
    return tenants == null ? emptyList() : unmodifiableList(tenants);
  }

}
