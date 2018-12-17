package io.serialized.client.projection;

import org.apache.commons.io.IOUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

@Path("/api-stub/projections/")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class ProjectionApiStub {

  private final Callback callback;

  public ProjectionApiStub(Callback callback) {
    this.callback = callback;
  }

  @GET
  @Path("single/{projectionName}/{id}")
  public Response getSingleProjection(@PathParam("projectionName") String projectionName, @PathParam("id") String id) throws IOException {
    String responseBody = getResource("single_projection.json");
    return Response.ok(responseBody, APPLICATION_JSON_TYPE).build();
  }

  @GET
  @Path("aggregated/{projectionName}")
  public Response getAggregatedProjection(@PathParam("projectionName") String projectionName) throws IOException {
    String responseBody = getResource("aggregated_projection.json");
    return Response.ok(responseBody, APPLICATION_JSON_TYPE).build();
  }

  @PUT
  @Path("definitions/{projectionName}")
  public Response createProjection(@PathParam("projectionName") String projectionName, CreateProjectionDefinitionRequest definition) {
    callback.projectionCreated(definition);
    return Response.ok(APPLICATION_JSON_TYPE).build();
  }

  private String getResource(String s) throws IOException {
    return IOUtils.toString(getClass().getResourceAsStream(s), "UTF-8");
  }

  public interface Callback {

    void projectionCreated(CreateProjectionDefinitionRequest request);

  }

}
