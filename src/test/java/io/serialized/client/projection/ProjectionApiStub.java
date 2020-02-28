package io.serialized.client.projection;

import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.OptionalInt;
import java.util.UUID;

import static io.serialized.client.SerializedOkHttpClient.SERIALIZED_TENANT_ID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

@Path("/api-stub/projections/")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class ProjectionApiStub {

  private final ProjectionApiCallback callback;

  public ProjectionApiStub(ProjectionApiCallback callback) {
    this.callback = callback;
  }

  @GET
  @Path("definitions")
  public Response listDefinitions() {
    Object response = callback.definitionsFetched();
    return Response.ok(APPLICATION_JSON_TYPE).entity(response).build();
  }

  @POST
  @Path("definitions")
  public Response createDefinition(ProjectionDefinition definition) {
    callback.definitionCreated(definition);
    return Response.ok(APPLICATION_JSON_TYPE).build();
  }

  @GET
  @Path("definitions/{projectionName}")
  public Response getDefinition(@PathParam("projectionName") String projectionName) {
    Object definition = callback.definitionFetched();
    return Response.ok(APPLICATION_JSON_TYPE).entity(definition).build();
  }

  @PUT
  @Path("definitions/{projectionName}")
  public Response createOrUpdateDefinition(@PathParam("projectionName") String projectionName, ProjectionDefinition definition) {
    callback.definitionUpdated(definition);
    return Response.ok(APPLICATION_JSON_TYPE).build();
  }

  @DELETE
  @Path("definitions/{projectionName}")
  public Response deleteDefinition(@PathParam("projectionName") String projectionName) {
    callback.definitionDeleted(projectionName);
    return Response.ok(APPLICATION_JSON_TYPE).build();
  }

  @GET
  @Path("aggregated/{projectionName}")
  public Response getAggregatedProjection(@PathParam("projectionName") String projectionName) {
    Object response = callback.aggregatedProjectionFetched(projectionName);
    return Response.ok(APPLICATION_JSON_TYPE).entity(response).build();
  }

  @DELETE
  @Path("aggregated/{projectionName}")
  public Response deleteAggregatedProjections(@PathParam("projectionName") String projectionName) {
    callback.aggregatedProjectionsDeleted(projectionName);
    return Response.ok(APPLICATION_JSON_TYPE).build();
  }

  @GET
  @Path("single/{projectionName}")
  public Response listSingleProjections(@PathParam("projectionName") String projectionName,
                                        @QueryParam("reference") String reference,
                                        @QueryParam("sort") @DefaultValue("createdAt") String sort,
                                        @QueryParam("skip") @DefaultValue("0") OptionalInt skip,
                                        @QueryParam("limit") @DefaultValue("100") @Min(1) @Max(1000) OptionalInt limit) {

    Object responseBody = callback.singleProjectionsFetched(projectionName, reference, sort, skip.getAsInt(), limit.getAsInt());
    return Response.ok(APPLICATION_JSON_TYPE).entity(responseBody).build();
  }

  @DELETE
  @Path("single/{projectionName}")
  public Response deleteSingleProjections(@PathParam("projectionName") String projectionName) {
    callback.singleProjectionsDeleted(projectionName);
    return Response.ok(APPLICATION_JSON_TYPE).build();
  }

  @GET
  @Path("single/{projectionName}/{id}")
  public Response getSingleProjection(@PathParam("projectionName") String projectionName, @PathParam("id") String id, @HeaderParam(SERIALIZED_TENANT_ID) String tenantId) {
    if (StringUtils.isNotBlank(tenantId)) {
      Object responseBody = callback.singleProjectionFetched(projectionName, id, UUID.fromString(tenantId));
      return Response.ok(APPLICATION_JSON_TYPE).entity(responseBody).build();
    } else {
      Object responseBody = callback.singleProjectionFetched(projectionName, id);
      return Response.ok(APPLICATION_JSON_TYPE).entity(responseBody).build();
    }
  }

  @GET
  public Response getProjectionsOverview() {
    Object responseBody = callback.overviewFetched();
    return Response.ok(APPLICATION_JSON_TYPE).entity(responseBody).build();
  }

  public interface ProjectionApiCallback {

    void definitionCreated(ProjectionDefinition definition);

    void definitionUpdated(ProjectionDefinition definition);

    void definitionDeleted(String projectionName);

    Object definitionFetched();

    Object definitionsFetched();

    Object overviewFetched();

    Object aggregatedProjectionFetched(String projectionName);

    Object singleProjectionsFetched(String projectionName, String reference, String sort, int skip, int limit);

    Object singleProjectionFetched(String projectionName, String id);

    Object singleProjectionFetched(String projectionName, String id, UUID tenantId);

    void singleProjectionsDeleted(String projectionName);

    void aggregatedProjectionsDeleted(String projectionName);

  }

}
