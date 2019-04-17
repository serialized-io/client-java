package io.serialized.client.projection;

import org.apache.commons.io.IOUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

import static io.serialized.client.projection.ProjectionDefinitions.newDefinitionList;
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
  @Path("definitions")
  public Response listDefinitions() {
    List<ProjectionDefinition> definitions = callback.definitionsFetched();
    return Response.ok(APPLICATION_JSON_TYPE).entity(newDefinitionList(definitions)).build();
  }

  @GET
  @Path("definitions/{projectionName}")
  public Response getDefinition(@PathParam("projectionName") String projectionName) {
    ProjectionDefinition definition = callback.definitionFetched();
    return Response.ok(APPLICATION_JSON_TYPE).entity(definition).build();
  }

  @PUT
  @Path("definitions/{projectionName}")
  public Response createDefinition(@PathParam("projectionName") String projectionName, ProjectionDefinition definition) {
    callback.definitionCreated(definition);
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
  public Response getAggregatedProjection(@PathParam("projectionName") String projectionName) throws IOException {
    String responseBody = getResource("aggregated_projection.json");
    return Response.ok(responseBody, APPLICATION_JSON_TYPE).build();
  }

  @GET
  @Path("single/{projectionName}/{id}")
  public Response getSingleProjection(@PathParam("projectionName") String projectionName, @PathParam("id") String id) throws IOException {
    String responseBody = getResource("single_projection.json");
    return Response.ok(responseBody, APPLICATION_JSON_TYPE).build();
  }

  private String getResource(String s) throws IOException {
    return IOUtils.toString(getClass().getResourceAsStream(s), "UTF-8");
  }

  public interface Callback {

    void definitionCreated(ProjectionDefinition request);

    void definitionDeleted(String projectionName);

    ProjectionDefinition definitionFetched();

    List<ProjectionDefinition> definitionsFetched();
  }

}
