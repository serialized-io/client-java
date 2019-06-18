package io.serialized.client.aggregate;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Path("/api-stub/aggregates")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class AggregateApiStub {

  private final AggregateApiCallback callback;

  public AggregateApiStub(AggregateApiCallback aggregateApiCallback) {
    this.callback = aggregateApiCallback;
  }

  @POST
  @Path("{aggregateType}/{aggregateId}/events")
  public Response saveEvents(@PathParam("aggregateType") String aggregateType,
                             @PathParam("aggregateId") String aggregateId,
                             @NotNull @Valid EventBatch eventBatch) {

    int status = callback.eventsStored(UUID.fromString(aggregateId), eventBatch);
    return Response.status(status).build();
  }

  @GET
  @Path("{aggregateType}/{aggregateId}")
  public Response loadAggregate(@PathParam("aggregateType") String aggregateType, @PathParam("aggregateId") String aggregateId) {
    Object responseBody = callback.aggregateLoaded(aggregateType, aggregateId);
    return Response.ok(APPLICATION_JSON_TYPE).entity(responseBody).build();
  }

  @HEAD
  @Path("{aggregateType}/{aggregateId}")
  public Response checkAggregate(@PathParam("aggregateType") String aggregateType, @PathParam("aggregateId") String aggregateId) {
    boolean exists = callback.aggregateChecked(aggregateType, aggregateId);
    return exists ? Response.ok(APPLICATION_JSON_TYPE).build() : Response.status(NOT_FOUND).build();
  }

  @DELETE
  @Path("{aggregateType}/{aggregateId}")
  public Response deleteAggregate(@PathParam("aggregateType") String aggregateType, @PathParam("aggregateId") String aggregateId, @QueryParam("deleteToken") String deleteToken) {
    if (isBlank(deleteToken)) {
      Map tokenResponse = callback.aggregateDeleteRequested(aggregateType, aggregateId);
      return Response.ok(APPLICATION_JSON_TYPE).entity(tokenResponse).build();
    } else {
      callback.aggregateDeletePerformed(aggregateType, aggregateId, deleteToken);
      return Response.ok(APPLICATION_JSON_TYPE).build();
    }
  }

  @DELETE
  @Path("{aggregateType}")
  public Response deleteAggregate(@PathParam("aggregateType") String aggregateType, @QueryParam("deleteToken") String deleteToken) {
    if (isBlank(deleteToken)) {
      Map tokenResponse = callback.aggregateTypeDeleteRequested(aggregateType);
      return Response.ok(APPLICATION_JSON_TYPE).entity(tokenResponse).build();
    } else {
      callback.aggregateTypeDeletePerformed(aggregateType, deleteToken);
      return Response.ok(APPLICATION_JSON_TYPE).build();
    }
  }

  public interface AggregateApiCallback {

    int eventsStored(UUID aggregateId, EventBatch eventBatch);

    Object aggregateLoaded(String aggregateType, String aggregateId);

    boolean aggregateChecked(String aggregateType, String aggregateId);

    Map aggregateDeleteRequested(String aggregateType, String aggregateId);

    void aggregateDeletePerformed(String aggregateType, String aggregateId, String deleteToken);

    Map aggregateTypeDeleteRequested(String aggregateType);

    void aggregateTypeDeletePerformed(String aggregateType, String deleteToken);

  }

}
