package io.serialized.client.tenant;

import java.util.List;

import static java.util.Collections.unmodifiableList;

public class TenantsResponse {

  private List<Tenant> tenants;

  public List<Tenant> tenants() {
    return unmodifiableList(tenants);
  }

}
