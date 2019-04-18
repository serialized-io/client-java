package io.serialized.client.feed;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

@Path("/api-stub/feeds")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class FeedApiStub {

  private FeedApiCallback callback;

  public FeedApiStub(FeedApiCallback callback) {
    this.callback = callback;
  }

  @GET
  public Response listFeeds() {
    Object responseBody = callback.feedOverviewLoaded();
    return Response.ok(APPLICATION_JSON_TYPE).entity(responseBody).build();
  }

  @HEAD
  @Path("_all")
  public Response getCurrentGlobalSequenceNumber() {
    int sequenceNumber = callback.currentGlobalSequenceNumberRequested();
    return Response.ok(APPLICATION_JSON_TYPE).header("Serialized-SequenceNumber-Current", sequenceNumber).build();
  }

  @GET
  @Path("_all")
  public Response getAllFeed() {
    Object feed = callback.allFeedLoaded();
    return Response.ok(APPLICATION_JSON_TYPE).entity(feed).build();
  }

  @GET
  @Path("{feedName}")
  public Response feedEntries(@PathParam("feedName") String feedName) {
    Object responseBody = callback.feedEntriesLoaded(feedName);
    return Response.ok(APPLICATION_JSON_TYPE).entity(responseBody).build();
  }

  public interface FeedApiCallback {

    Object feedOverviewLoaded();

    Object feedEntriesLoaded(String feedName);

    int currentGlobalSequenceNumberRequested();

    Object allFeedLoaded();
  }


}

