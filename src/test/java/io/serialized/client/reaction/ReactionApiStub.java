package io.serialized.client.reaction;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

@Path("/api-stub/reactions/")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class ReactionApiStub {

  private final ReactionApiCallback callback;

  public ReactionApiStub(ReactionApiCallback callback) {
    this.callback = callback;
  }

  @POST
  @Path("definitions")
  public Response createDefinition(ReactionDefinition definition) {
    callback.definitionCreated(definition);
    return Response.ok(APPLICATION_JSON_TYPE).build();
  }

  @GET
  @Path("definitions")
  public Response listDefinitions() {
    Object definitions = callback.definitionsFetched();
    return Response.ok(APPLICATION_JSON_TYPE).entity(definitions).build();
  }

  @GET
  @Path("definitions/{reactionName}")
  public Response getDefinition(@PathParam("reactionName") String reactionName) {
    Object definition = callback.definitionFetched();
    return Response.ok(APPLICATION_JSON_TYPE).entity(definition).build();
  }

  @PUT
  @Path("definitions/{reactionName}")
  public Response createDefinition(@PathParam("reactionName") String reactionName, ReactionDefinition definition) {
    callback.definitionUpdated(definition);
    return Response.ok(APPLICATION_JSON_TYPE).build();
  }

  @DELETE
  @Path("definitions/{reactionName}")
  public Response deleteDefinition(@PathParam("reactionName") String reactionName) {
    callback.definitionDeleted(reactionName);
    return Response.ok(APPLICATION_JSON_TYPE).build();
  }

  @GET
  @Path("scheduled")
  public Response listScheduled() {
    Object definitions = callback.scheduledReactionsFetched();
    return Response.ok(APPLICATION_JSON_TYPE).entity(definitions).build();
  }

  @GET
  @Path("triggered")
  public Response listTriggered() {
    Object definitions = callback.triggeredReactionsFetched();
    return Response.ok(APPLICATION_JSON_TYPE).entity(definitions).build();
  }

  public interface ReactionApiCallback {

    void definitionCreated(ReactionDefinition definition);

    void definitionUpdated(ReactionDefinition request);

    Object definitionFetched();

    Object definitionsFetched();

    void definitionDeleted(String reactionName);

    Object scheduledReactionsFetched();

    Object triggeredReactionsFetched();

  }

}
