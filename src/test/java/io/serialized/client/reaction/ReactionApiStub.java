package io.serialized.client.reaction;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

import static io.serialized.client.reaction.ReactionDefinitions.newDefinitionList;
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

  @GET
  @Path("definitions")
  public Response listDefinitions() {
    List<ReactionDefinition> definitions = callback.definitionsFetched();
    return Response.ok(APPLICATION_JSON_TYPE).entity(newDefinitionList(definitions)).build();
  }

  @GET
  @Path("definitions/{reactionName}")
  public Response getDefinition(@PathParam("reactionName") String reactionName) {
    ReactionDefinition definition = callback.definitionFetched();
    return Response.ok(APPLICATION_JSON_TYPE).entity(definition).build();
  }

  @PUT
  @Path("definitions/{reactionName}")
  public Response createDefinition(@PathParam("reactionName") String reactionName, ReactionDefinition definition) {
    callback.definitionCreated(definition);
    return Response.ok(APPLICATION_JSON_TYPE).build();
  }

  @DELETE
  @Path("definitions/{reactionName}")
  public Response deleteDefinition(@PathParam("reactionName") String reactionName) {
    callback.definitionDeleted(reactionName);
    return Response.ok(APPLICATION_JSON_TYPE).build();
  }

  public interface Callback {

    void definitionCreated(ReactionDefinition request);

    ReactionDefinition definitionFetched();

    List<ReactionDefinition> definitionsFetched();

    void definitionDeleted(String reactionName);
  }

}
