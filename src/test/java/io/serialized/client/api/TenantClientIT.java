package io.serialized.client.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import io.dropwizard.testing.junit.DropwizardClientRule;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.tenant.Tenant;
import io.serialized.client.tenant.TenantApiStub;
import io.serialized.client.tenant.TenantClient;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static io.serialized.client.tenant.Tenant.newTenant;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TenantClientIT {

  private final TenantApiStub.TenantApiCallback apiCallback = mock(TenantApiStub.TenantApiCallback.class);

  @Rule
  public final DropwizardClientRule dropwizard = new DropwizardClientRule(new TenantApiStub(apiCallback));

  @Before
  public void setUp() {
    dropwizard.getObjectMapper().setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
  }

  @Test
  public void testAddTenant() {

    TenantClient tenantClient = getTenantClient();

    UUID tenantId = UUID.randomUUID();

    Tenant tenant = newTenant(tenantId)
        .reference("my-ref")
        .build();

    tenantClient.addTenant(tenant);

    ArgumentCaptor<Tenant> captor = ArgumentCaptor.forClass(Tenant.class);
    verify(apiCallback, times(1)).tenantAdded(captor.capture());

    Tenant value = captor.getValue();
    assertThat(value.tenantId(), is(tenantId.toString()));
    assertThat(value.reference(), is("my-ref"));
  }

  @Test
  public void testDeleteTenant() {

    TenantClient tenantClient = getTenantClient();

    UUID tenantId = UUID.randomUUID();
    tenantClient.deleteTenant(tenantId);
    verify(apiCallback, times(1)).tenantDeleted(tenantId);
  }

  @Test
  public void testListTenants() throws IOException {

    TenantClient tenantClient = getTenantClient();

    when(apiCallback.tenantsLoaded()).thenReturn(getResource("/tenant/tenants.json"));

    List<Tenant> tenants = tenantClient.listTenants();
    assertThat(tenants.size(), is(1));
    assertThat(tenants.get(0).tenantId(), is("a8c929ac-b59d-429b-8570-99c9a84f6b2c"));
    assertThat(tenants.get(0).tenantNumber(), is("1"));
    assertThat(tenants.get(0).addedAt(), is(1568105144988L));
    assertThat(tenants.get(0).deleted(), is(false));
  }

  private TenantClient getTenantClient() {
    return TenantClient.tenantClient(
        SerializedClientConfig.serializedConfig()
            .rootApiUrl(dropwizard.baseUri() + "/api-stub/")
            .accessKey("aaaaa")
            .secretAccessKey("bbbbb")
            .build())
        .build();
  }

  private String getResource(String resource) throws IOException {
    return IOUtils.toString(getClass().getResourceAsStream(resource), "UTF-8");
  }

}
