package io.serialized.client.aggregates;

import org.apache.commons.io.IOUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

@Path("/api-stub/aggregates")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class AggregatesApi {

  private final Callback callback;

  public AggregatesApi(Callback callback) {
    this.callback = callback;
  }

  @POST
  @Path("{aggregateType}/events")
  public Response saveEvents(@PathParam("aggregateType") String aggregateType, @NotNull @Valid EventBatchDto eventBatch) {
    callback.eventsStored(eventBatch);
    return Response.ok().build();
  }

  @GET
  @Path("order/{aggregateId}")
  public Response loadAggregate(@PathParam("aggregateId") String aggregateId) throws IOException {
    String responseBody = getResource("load_aggregate.json");
    return Response.ok(responseBody, APPLICATION_JSON_TYPE).build();
  }

  @GET
  @Path("order-specific/{aggregateId}")
  public Response loadAggregateWithSpecifiedEventNamed(@PathParam("aggregateId") String aggregateId) throws IOException {
    String responseBody = getResource("load_aggregate_not_classname.json");
    return Response.ok(responseBody, APPLICATION_JSON_TYPE).build();
  }

  public interface Callback {

    void eventsStored(EventBatchDto eventBatch);

  }

  private String getResource(String s) throws IOException {
    return IOUtils.toString(getClass().getResourceAsStream(s), "UTF-8");
  }
}
