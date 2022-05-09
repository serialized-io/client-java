package io.serialized.client.tenant;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.SerializedOkHttpClient;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;

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

  public void updateTenant(Tenant tenant) {
    HttpUrl url = apiRoot.newBuilder().addPathSegment("tenants").addPathSegment(tenant.tenantId().toString()).build();
    client.put(url, tenant);
  }

  public static class Builder {

    private final ObjectMapper objectMapper = new ObjectMapper()
        .disable(FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(FAIL_ON_EMPTY_BEANS)
        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        .setSerializationInclusion(NON_NULL);

    private final OkHttpClient httpClient;
    private final HttpUrl apiRoot;

    public Builder(SerializedClientConfig config) {
      this.httpClient = config.newHttpClient();
      this.apiRoot = config.apiRoot();
    }

    /**
     * Allows object mapper customization.
     */
    public Builder configureObjectMapper(Consumer<ObjectMapper> consumer) {
      consumer.accept(objectMapper);
      return this;
    }

    public TenantClient build() {
      return new TenantClient(this);
    }

  }

}
