package io.serialized.client.feed;

import io.dropwizard.testing.junit.DropwizardClientRule;
import io.serialized.client.SerializedClientConfig;
import org.apache.commons.io.IOUtils;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class FeedApiClientTest {

  @ClassRule
  public static final DropwizardClientRule DROPWIZARD = new DropwizardClientRule(new SerializedAggregatesApiStub());

  @Path("/api-stub/feeds")
  @Produces(APPLICATION_JSON)
  @Consumes(APPLICATION_JSON)
  public static class SerializedAggregatesApiStub {

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

  private FeedApiClient feedClient = FeedApiClient.feedClient(
      SerializedClientConfig.builder()
          .rootApiUrl(DROPWIZARD.baseUri() + "/api-stub/")
          .accessKey("aaaaa")
          .secretAccessKey("bbbbb")
          .build())
      .build();

  @Test
  public void listFeeds() throws IOException {
    assertThat(feedClient.listFeeds().size(), is(1));
    assertThat(feedClient.listFeeds().get(0).aggregateType(), is("games"));
    assertThat(feedClient.listFeeds().get(0).aggregateCount(), is(10L));
    assertThat(feedClient.listFeeds().get(0).batchCount(), is(48L));
    assertThat(feedClient.listFeeds().get(0).eventCount(), is(96L));
  }

  @Test
  public void feedEntries() throws IOException {
    FeedResponse feedResponse = feedClient.feed("games");

    assertThat(feedResponse.entries().size(), is(48));
    assertThat(feedResponse.events().size(), is(96));
  }
}