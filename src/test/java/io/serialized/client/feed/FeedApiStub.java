package io.serialized.client.feed;

import io.dropwizard.jersey.params.IntParam;
import io.dropwizard.jersey.params.LongParam;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
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
  public Response feedEntries(@PathParam("feedName") String feedName,
                              @QueryParam("before") @DefaultValue("0") LongParam before,
                              @QueryParam("since") @DefaultValue("0") LongParam since,
                              @QueryParam("limit") @DefaultValue("1000") @Min(1) @Max(1000) IntParam limit) {
    QueryParams queryParams = new QueryParams(limit.get(), since.get(), before.get());
    Object responseBody = callback.feedEntriesLoaded(feedName, queryParams);
    return Response.ok(APPLICATION_JSON_TYPE).entity(responseBody).build();
  }

  public class QueryParams {

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

    int currentGlobalSequenceNumberRequested();

    Object allFeedLoaded();
  }


}

