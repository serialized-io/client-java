package io.serialized.client.tenant;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Map;

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
  public Response createTenant(Map tenant) {
    callback.tenantCreated(tenant);
    return Response.ok(APPLICATION_JSON_TYPE).build();
  }

  @DELETE
  @Path("{tenantId}")
  public Response deleteTenant(@PathParam("tenantId") String tenantId) {
    callback.tenantDeleted(tenantId);
    return Response.ok(APPLICATION_JSON_TYPE).build();
  }

  public interface TenantApiCallback {

    void tenantCreated(Map tenant);

    void tenantDeleted(String tenantId);

  }

}
