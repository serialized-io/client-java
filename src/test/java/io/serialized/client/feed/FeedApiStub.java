package io.serialized.client.feed;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

@Path("/api-stub/feeds")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class FeedApiStub {

  private final FeedApiCallback callback;

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
    long sequenceNumber = callback.currentGlobalSequenceNumberRequested();
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
  public Response feedEntries(@PathParam("feedName") String feedName,
                              @QueryParam("before") @DefaultValue("0") long before,
                              @QueryParam("since") @DefaultValue("0") long since,
                              @QueryParam("limit") @DefaultValue("1000") @Min(1) @Max(1000) int limit) {

    QueryParams queryParams = new QueryParams(limit, since, before);
    Object responseBody = callback.feedEntriesLoaded(feedName, queryParams);
    return Response.ok(APPLICATION_JSON_TYPE).entity(responseBody).build();
  }

  @HEAD
  @Path("{feedName}")
  public Response getCurrentSequenceNumber(@PathParam("feedName") String feedName) {
    long sequenceNumber = callback.currentSequenceNumberRequested();
    return Response.ok(APPLICATION_JSON_TYPE).header("Serialized-SequenceNumber-Current", sequenceNumber).build();
  }

  public static class QueryParams {

    private final Integer limit;
    private final Long since;
    private final Long before;

    public QueryParams(Integer limit, Long since, Long before) {
      this.limit = limit;
      this.since = since;
      this.before = before;
    }

    public Integer getLimit() {
      return limit;
    }

    public Long getSince() {
      return since;
    }

    public Long getBefore() {
      return before;
    }
  }

  public interface FeedApiCallback {

    Object feedOverviewLoaded();

    Object feedEntriesLoaded(String feedName, QueryParams queryParams);

    long currentSequenceNumberRequested();

    long currentGlobalSequenceNumberRequested();

    Object allFeedLoaded();

  }

}
