package io.serialized.client.aggregate;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

@Path("/api-stub/aggregates")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class AggregateApiStub {

  private final Callback callback;

  public AggregateApiStub(Callback callback) {
    this.callback = callback;
  }

  @POST
  @Path("{aggregateType}/events")
  public Response saveEvents(@PathParam("aggregateType") String aggregateType, @NotNull @Valid EventBatchDto eventBatch) {
    callback.eventsStored(eventBatch);
    return Response.ok().build();
  }

  @GET
  @Path("{aggregateType}/{aggregateId}")
  public Response loadAggregate(@PathParam("aggregateType") String aggregateType, @PathParam("aggregateId") String aggregateId) {
    String responseBody = callback.aggregateLoaded(aggregateType, aggregateId);
    return Response.ok(responseBody, APPLICATION_JSON_TYPE).build();
  }

  public interface Callback {

    void eventsStored(EventBatchDto eventBatch);

    String aggregateLoaded(String aggregateType, String aggregateId);
  }

}
