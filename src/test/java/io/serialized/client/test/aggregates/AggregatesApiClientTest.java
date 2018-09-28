package io.serialized.client.test.aggregates;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.testing.junit.DropwizardClientRule;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.aggregates.AggregatesApiClient;
import io.serialized.client.aggregates.EventBatch;
import io.serialized.client.aggregates.LoadAggregateResponse;
import org.apache.commons.io.IOUtils;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.UUID;

import static io.serialized.client.aggregates.EventBatch.newEvent;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class AggregatesApiClientTest {

  public static class OrderPlacedEvent {

  }

  @ClassRule
  public static final DropwizardClientRule DROPWIZARD = new DropwizardClientRule(new SerializedAggregatesApiStub());

  @Path("/api-stub/aggregates")
  @Produces(APPLICATION_JSON)
  @Consumes(APPLICATION_JSON)
  public static class SerializedAggregatesApiStub {

    @POST
    @Path("{aggregateType}/events")
    public Response saveEvents(@PathParam("aggregateType") String aggregateType) {
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

    private String getResource(String s) throws IOException {
      return IOUtils.toString(getClass().getResourceAsStream(s), "UTF-8");
    }
  }

  private AggregatesApiClient.Builder aggregatesClientBuilder = AggregatesApiClient.aggregatesClient(
      SerializedClientConfig.builder()
          .rootApiUrl(DROPWIZARD.baseUri() + "/api-stub/")
          .accessKey("aaaaa")
          .secretAccessKey("bbbbb").build());

  @Test
  public void loadAggregate() throws IOException {
    AggregatesApiClient aggregatesApiClient = aggregatesClientBuilder
        .registerEventType(OrderPlacedEvent.class)
        .build();

    LoadAggregateResponse aggregateResponse = aggregatesApiClient.loadAggregate("order", "723ecfce-14e9-4889-98d5-a3d0ad54912f");

    assertThat(aggregateResponse.aggregateId(), is("723ecfce-14e9-4889-98d5-a3d0ad54912f"));
    assertThat(aggregateResponse.aggregateType(), is("order"));
    assertThat(aggregateResponse.aggregateVersion(), is(1L));
    assertThat(aggregateResponse.events().size(), is(1));
    assertThat(aggregateResponse.events().get(0).data().getClass().getSimpleName(), is(OrderPlacedEvent.class.getSimpleName()));
  }

  @Test
  public void loadAggregateWithSpecificedEventType() throws IOException {
    AggregatesApiClient aggregatesApiClient = aggregatesClientBuilder
        .registerEventType("order-placed", OrderPlacedEvent.class)
        .build();

    LoadAggregateResponse aggregateResponse = aggregatesApiClient.loadAggregate("order-specific", "723ecfce-14e9-4889-98d5-a3d0ad54912f");

    assertThat(aggregateResponse.aggregateId(), is("723ecfce-14e9-4889-98d5-a3d0ad54912f"));
    assertThat(aggregateResponse.aggregateType(), is("order-specific"));
    assertThat(aggregateResponse.aggregateVersion(), is(1L));
    assertThat(aggregateResponse.events().size(), is(1));
    assertThat(aggregateResponse.events().get(0).data().getClass().getSimpleName(), is(OrderPlacedEvent.class.getSimpleName()));
  }

  @Test
  public void saveEvents() throws IOException {

    AggregatesApiClient aggregatesApiClient = aggregatesClientBuilder
        .registerEventType(OrderPlacedEvent.class)
        .build();

    EventBatch eventBatch = EventBatch.newBatch()
        .aggregateId("723ecfce-14e9-4889-98d5-a3d0ad54912f")
        .addEvent(newEvent("OrderPlaced")
            .eventId(UUID.fromString("127b80b5-4a05-4774-b870-1c9a2e2a27a3"))
            .data(ImmutableMap.of(
                "customerId", "some-test-id-1",
                "orderAmount", 12345))
            .build()).build();

    aggregatesApiClient.storeEvents("order", eventBatch);
  }

}