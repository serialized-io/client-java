package io.serialized.client.tenant;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

@Path("/api-stub/tenants/")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class TenantApiStub {

  private final TenantApiCallback callback;

  public TenantApiStub(TenantApiCallback callback) {
    this.callback = callback;
  }

  @POST
  public Response createTenant(@Valid Tenant tenant) {
    callback.tenantAdded(tenant);
    return Response.ok(APPLICATION_JSON_TYPE).build();
  }

  @GET
  public Response listTenants() {
    Object responseBody = callback.tenantsLoaded();
    return Response.ok(APPLICATION_JSON_TYPE).entity(responseBody).build();
  }

  @DELETE
  @Path("{tenantId}")
  public Response deleteTenant(@PathParam("tenantId") String tenantId) {
    callback.tenantDeleted(UUID.fromString(tenantId));
    return Response.ok(APPLICATION_JSON_TYPE).build();
  }

  public interface TenantApiCallback {

    void tenantAdded(Tenant tenant);

    Object tenantsLoaded();

    void tenantDeleted(UUID tenantId);

  }

}
