package io.serialized.client.reaction;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

@Path("/api-stub/reactions/")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class ReactionApiStub {

  private final ReactionApiStub.Callback callback;

  public ReactionApiStub(ReactionApiStub.Callback callback) {
    this.callback = callback;
  }

  @PUT
  @Path("definitions/{reactionName}")
  public Response createReactionDefinition(@PathParam("reactionName") String reactionName, ReactionDefinition definition) {
    callback.reactionCreated(definition);
    return Response.ok(APPLICATION_JSON_TYPE).build();
  }

  @GET
  @Path("definitions/{reactionName}")
  public Response getReactionDefinition(@PathParam("reactionName") String reactionName) {
    ReactionDefinition definition = callback.reactionFetched();
    return Response.ok(APPLICATION_JSON_TYPE).entity(definition).build();
  }


  public interface Callback {

    void reactionCreated(ReactionDefinition request);

    ReactionDefinition reactionFetched();
  }

}
