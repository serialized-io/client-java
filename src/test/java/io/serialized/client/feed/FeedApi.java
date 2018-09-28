package io.serialized.client.feed;

import org.apache.commons.io.IOUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

@Path("/api-stub/feeds")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class FeedApi {

  @GET
  public Response listFeeds() throws IOException {
    String responseBody = getResource("feeds.json");
    return Response.ok(responseBody, APPLICATION_JSON_TYPE).build();
  }

  @GET
  @Path("{feedName}")
  public Response feedEntries(@PathParam("feedName") String feedName) throws IOException {
    String responseBody = getResource("feedentries.json");
    return Response.ok(responseBody, APPLICATION_JSON_TYPE).build();
  }

  private String getResource(String s) throws IOException {
    return IOUtils.toString(getClass().getResourceAsStream(s), "UTF-8");
  }
}

