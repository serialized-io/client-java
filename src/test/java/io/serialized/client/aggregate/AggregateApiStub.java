package io.serialized.client.aggregate;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.UUID;

import static io.serialized.client.SerializedOkHttpClient.SERIALIZED_TENANT_ID;
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
                             @HeaderParam(SERIALIZED_TENANT_ID) String tenantId,
                             @NotNull @Valid EventBatch eventBatch) {

    if (tenantId == null) {
      return Response.status(callback.eventsStored(UUID.fromString(aggregateId), eventBatch)).build();
    } else {
      return Response.status(callback.eventsStored(UUID.fromString(aggregateId), eventBatch, UUID.fromString(tenantId))).build();
    }

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

    int eventsStored(UUID aggregateId, EventBatch eventBatch, UUID tenantId);

    Object aggregateLoaded(String aggregateType, String aggregateId);

    boolean aggregateChecked(String aggregateType, String aggregateId);

    Map aggregateDeleteRequested(String aggregateType, String aggregateId);

    void aggregateDeletePerformed(String aggregateType, String aggregateId, String deleteToken);

    Map aggregateTypeDeleteRequested(String aggregateType);

    void aggregateTypeDeletePerformed(String aggregateType, String deleteToken);

  }

}
