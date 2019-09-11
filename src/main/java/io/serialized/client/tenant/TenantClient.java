package io.serialized.client.tenant;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.SerializedOkHttpClient;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import java.util.List;
import java.util.UUID;

public class TenantClient {

  private final SerializedOkHttpClient client;
  private final HttpUrl apiRoot;

  private TenantClient(Builder builder) {
    this.client = new SerializedOkHttpClient(builder.httpClient, builder.objectMapper);
    this.apiRoot = builder.apiRoot;
  }

  public static TenantClient.Builder tenantClient(SerializedClientConfig config) {
    return new TenantClient.Builder(config);
  }

  public void addTenant(Tenant tenant) {
    HttpUrl url = apiRoot.newBuilder().addPathSegment("tenants").build();
    client.post(url, tenant);
  }

  public List<Tenant> listTenants() {
    HttpUrl url = apiRoot.newBuilder().addPathSegment("tenants").build();
    return client.get(url, TenantsResponse.class).tenants();
  }

  public void deleteTenant(UUID tenantId) {
    HttpUrl url = apiRoot.newBuilder().addPathSegment("tenants").addPathSegment(tenantId.toString()).build();
    client.delete(url);
  }

  public static class Builder {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final HttpUrl apiRoot;

    Builder(SerializedClientConfig config) {
      this.httpClient = config.httpClient();
      this.objectMapper = config.objectMapper();
      this.apiRoot = config.apiRoot();
    }

    public TenantClient build() {
      return new TenantClient(this);
    }
  }

}
