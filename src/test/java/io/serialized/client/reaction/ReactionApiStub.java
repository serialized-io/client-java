package io.serialized.client.reaction;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
  @Path("/")
  public Response listReactions(@QueryParam("status") @DefaultValue("ALL") String status,
                                @QueryParam("skip") @DefaultValue("0") int skip,
                                @QueryParam("limit") @DefaultValue("10") @Min(1) @Max(100) int limit) {

    Object definitions = callback.reactionsListed(status, skip, limit);
    return Response.ok(APPLICATION_JSON_TYPE).entity(definitions).build();
  }

  @POST
  @Path("{reactionId}/execute")
  public Response execute(@PathParam("reactionId") String reactionId) {
    callback.reactionExecuted(reactionId);
    return Response.ok(APPLICATION_JSON_TYPE).build();
  }

  @DELETE
  @Path("{reactionId}")
  public Response deleteScheduled(@PathParam("reactionId") String reactionId) {
    callback.reactionDeleted(reactionId);
    return Response.ok(APPLICATION_JSON_TYPE).build();
  }

  public interface ReactionApiCallback {

    void definitionCreated(ReactionDefinition definition);

    void definitionUpdated(ReactionDefinition request);

    Object definitionFetched();

    Object definitionsFetched();

    void definitionDeleted(String reactionName);

    Object reactionsListed(String status, int skip, int limit);

    void reactionExecuted(String reactionId);

    void reactionDeleted(String reactionId);

  }

}
