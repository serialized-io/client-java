package io.serialized.client.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.testing.junit5.DropwizardClientExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.serialized.client.aggregate.AggregateApiStub;
import io.serialized.client.aggregate.EventBatch;
import io.serialized.client.feed.FeedApiStub;
import io.serialized.client.projection.ProjectionApiStub;
import io.serialized.client.projection.ProjectionDefinition;
import io.serialized.client.reaction.ReactionApiStub;
import io.serialized.client.reaction.ReactionDefinition;
import io.serialized.client.tenant.Tenant;
import io.serialized.client.tenant.TenantApiStub;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptySet;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
public class JerseyClientIT {

  private final AggregateApiStub.AggregateApiCallback aggregateApiCallback = mock(AggregateApiStub.AggregateApiCallback.class);
  private final FeedApiStub.FeedApiCallback feedApiCallback = mock(FeedApiStub.FeedApiCallback.class);
  private final ReactionApiStub.ReactionApiCallback reactionApiCallback = mock(ReactionApiStub.ReactionApiCallback.class);
  private final ProjectionApiStub.ProjectionApiCallback projectionApiCallback = mock(ProjectionApiStub.ProjectionApiCallback.class);
  private final TenantApiStub.TenantApiCallback tenantApiCallback = mock(TenantApiStub.TenantApiCallback.class);

  public final DropwizardClientExtension dropwizardRule = new DropwizardClientExtension(
      new AggregateApiStub(aggregateApiCallback),
      new FeedApiStub(feedApiCallback),
      new ReactionApiStub(reactionApiCallback),
      new ProjectionApiStub(projectionApiCallback),
      new TenantApiStub(tenantApiCallback)
  );

  @BeforeEach
  public void setUp() {
    dropwizardRule.getObjectMapper().setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
  }

  @Test
  public void testLoadAggregate() {

    UUID aggregateId = UUID.fromString("99415be8-6819-4470-860c-c2933558d8d3");
    when(aggregateApiCallback.aggregateLoaded("order", aggregateId, 0, 1000)).thenReturn(ImmutableMap.of("apa", "banan"));

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    Map aggregateResponse = client.target(apiRoot)
        .path("aggregates")
        .path("order")
        .path(aggregateId.toString())
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .get(Map.class);

    assertThat(aggregateResponse.get("apa")).isEqualTo("banan");
  }

  @Test
  public void testCheckAggregate() {

    UUID aggregateId = UUID.fromString("99415be8-6819-4470-860c-c2933558d8d3");
    when(aggregateApiCallback.aggregateChecked("order", aggregateId)).thenReturn(true);

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    Response response = client.target(apiRoot)
        .path("aggregates")
        .path("order")
        .path(aggregateId.toString())
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .head();

    verify(aggregateApiCallback, times(1)).aggregateChecked("order", aggregateId);
    assertThat(response.getStatusInfo().getFamily()).isEqualTo(SUCCESSFUL);
  }

  @Test
  public void testDeleteAggregate() {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    String expectedToken = UUID.randomUUID().toString();
    UUID aggregateId = UUID.fromString("99415be8-6819-4470-860c-c2933558d8d3");
    when(aggregateApiCallback.aggregateDeleteRequested("order", aggregateId)).thenReturn(ImmutableMap.of("deleteToken", expectedToken));

    Map deleteTokenResponse = client.target(apiRoot)
        .path("aggregates")
        .path("order")
        .path(aggregateId.toString())
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .delete(Map.class);

    String deleteToken = (String) deleteTokenResponse.get("deleteToken");

    Response response = client.target(apiRoot)
        .path("aggregates")
        .path("order")
        .path(aggregateId.toString())
        .queryParam("deleteToken", deleteToken)
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .delete();

    verify(aggregateApiCallback, times(1)).aggregateDeletePerformed("order", aggregateId, expectedToken);
    assertThat(response.getStatusInfo().getFamily()).isEqualTo(SUCCESSFUL);
  }

  @Test
  public void testDeleteAggregateType() {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    String expectedToken = UUID.randomUUID().toString();
    when(aggregateApiCallback.aggregateTypeDeleteRequested("order")).thenReturn(ImmutableMap.of("deleteToken", expectedToken));

    Map deleteTokenResponse = client.target(apiRoot)
        .path("aggregates")
        .path("order")
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .delete(Map.class);

    String deleteToken = (String) deleteTokenResponse.get("deleteToken");

    Response response = client.target(apiRoot)
        .path("aggregates")
        .path("order")
        .queryParam("deleteToken", deleteToken)
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .delete();

    verify(aggregateApiCallback, times(1)).aggregateTypeDeletePerformed("order", expectedToken);
    assertThat(response.getStatusInfo().getFamily()).isEqualTo(SUCCESSFUL);
  }

  @Test
  public void testStoreEvents() {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    UUID aggregateId = UUID.fromString("3070b6fb-f31b-4a8e-bc03-e22d38f4076e");
    Map eventBatch = ImmutableMap.of(
        "events", ImmutableList.of(
            ImmutableMap.of(
                "eventId", "",
                "eventType", "PaymentProcessed",
                "data", ImmutableMap.of(
                    "paymentMethod", "CARD",
                    "amount", "1000",
                    "currency", "SEK"
                )
            )
        ),
        "expectedVersion", "0"
    );

    when(aggregateApiCallback.eventsStored(eq(aggregateId), any(EventBatch.class))).thenReturn(OK);

    Response response = client.target(apiRoot)
        .path("aggregates")
        .path("order")
        .path(aggregateId.toString())
        .path("events")
        .request(MediaType.APPLICATION_JSON_TYPE)
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .post(Entity.json(eventBatch));

    verify(aggregateApiCallback, times(1)).eventsStored(eq(aggregateId), any(EventBatch.class));

    assertThat(response.getStatusInfo().getFamily()).isEqualTo(SUCCESSFUL);
  }

  @Test
  public void testGetFeedOverview() throws IOException {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    when(feedApiCallback.feedOverviewLoaded()).thenReturn(getResource("/feed/feeds.json"));

    Map response = client.target(apiRoot)
        .path("feeds")
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .get(Map.class);

    List<Map> feeds = (List<Map>) response.get("feeds");
    assertThat(feeds).hasSize(1);
  }

  @Test
  public void testGetFeedEntries() throws IOException {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();
    String feedName = "orders";

    ArgumentCaptor<FeedApiStub.QueryParams> queryParams = ArgumentCaptor.forClass(FeedApiStub.QueryParams.class);
    when(feedApiCallback.feedEntriesLoaded(eq(feedName), queryParams.capture())).thenReturn(getResource("/feed/feedentries.json"));

    Map response = client.target(apiRoot)
        .path("feeds")
        .path(feedName)
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .get(Map.class);

    List entries = (List) response.get("entries");
    assertThat(entries).hasSize(48);
  }

  @Test
  public void testCurrentGlobalSequenceNumber() {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    Long sequenceNumber = 20L;
    when(feedApiCallback.currentGlobalSequenceNumberRequested()).thenReturn(sequenceNumber);

    Response response = client.target(apiRoot)
        .path("feeds")
        .path("_all")
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .head();

    Long globalSequenceNumber = Long.parseLong((String) response.getHeaders().getFirst("Serialized-SequenceNumber-Current"));
    assertThat(globalSequenceNumber).isEqualTo(sequenceNumber);
  }

  @Test
  public void testCurrentSequenceNumber() {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    Long sequenceNumber = 20L;
    String feedName = "payment";
    when(feedApiCallback.currentSequenceNumberRequested(feedName)).thenReturn(sequenceNumber);

    Response response = client.target(apiRoot)
        .path("feeds")
        .path(feedName)
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .head();

    Long globalSequenceNumber = Long.parseLong((String) response.getHeaders().getFirst("Serialized-SequenceNumber-Current"));
    assertThat(globalSequenceNumber).isEqualTo(sequenceNumber);
  }

  @Test
  public void testGetAllFeed() throws IOException {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    when(feedApiCallback.allFeedLoaded(emptySet())).thenReturn(getResource("/feed/allFeed.json"));

    Map response = client.target(apiRoot)
        .path("feeds")
        .path("_all")
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .get(Map.class);

    List<Map> entries = (List<Map>) response.get("entries");
    assertThat(entries.size()).isEqualTo(1);
  }

  @Test
  public void testListReactionDefinitions() throws IOException {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    when(reactionApiCallback.definitionsFetched()).thenReturn(getResource("/reaction/listDefinitions.json"));

    Map response = client.target(apiRoot)
        .path("reactions")
        .path("definitions")
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .get(Map.class);

    List<Map> definitions = (List<Map>) response.get("definitions");
    assertThat(definitions).hasSize(1);
    assertThat(definitions.get(0).get("feedName")).isEqualTo("payment");
  }

  @Test
  public void testCreateReactionDefinition() {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    Map<String, Object> reactionDefinition = ImmutableMap.of(
        "reactionName", "payment-processed-email-reaction",
        "feedName", "payment",
        "reactOnEventType", "PaymentProcessed",
        "action", ImmutableMap.of(
            "actionType", "HTTP_POST",
            "targetUri", "https://your-email-service"
        )
    );

    Response response = client.target(apiRoot)
        .path("reactions")
        .path("definitions")
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .post(Entity.json(reactionDefinition));

    verify(reactionApiCallback, times(1)).definitionCreated(any(ReactionDefinition.class));
    assertThat(response.getStatusInfo().getFamily()).isEqualTo(SUCCESSFUL);
  }

  @Test
  public void testCreateOrUpdateReactionDefinition() {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    Map<String, Object> reactionDefinition = ImmutableMap.of(
        "reactionName", "payment-processed-email-reaction",
        "feedName", "payment",
        "reactOnEventType", "PaymentProcessed",
        "action", ImmutableMap.of(
            "actionType", "HTTP_POST",
            "targetUri", "https://your-email-service"
        )
    );

    Response response = client.target(apiRoot)
        .path("reactions")
        .path("definitions")
        .path("payment-processed-email-reaction")
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .put(Entity.json(reactionDefinition));

    verify(reactionApiCallback, times(1)).definitionUpdated(any(ReactionDefinition.class));
    assertThat(response.getStatusInfo().getFamily()).isEqualTo(SUCCESSFUL);
  }

  @Test
  public void testDeleteReactionDefinition() {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    Response response = client.target(apiRoot)
        .path("reactions")
        .path("definitions")
        .path("payment-notifier")
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .delete();

    verify(reactionApiCallback, times(1)).definitionDeleted("payment-notifier");
    assertThat(response.getStatusInfo().getFamily()).isEqualTo(SUCCESSFUL);
  }

  @Test
  public void testGetReactionDefinition() {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    Map<String, Object> reactionDefinition = ImmutableMap.of(
        "reactionName", "payment-processed-email-reaction",
        "feedName", "payment",
        "reactOnEventType", "PaymentProcessed",
        "action", ImmutableMap.of(
            "actionType", "HTTP_POST",
            "targetUri", "https://your-email-service"
        )
    );

    when(reactionApiCallback.definitionFetched()).thenReturn(reactionDefinition);

    Map response = client.target(apiRoot)
        .path("reactions")
        .path("definitions")
        .path("payment-notifier")
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .get(Map.class);


    assertThat(response.get("reactionName")).isEqualTo("payment-processed-email-reaction");
  }

  @Test
  public void testGetProjectionsOverview() throws IOException {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    when(projectionApiCallback.overviewFetched()).thenReturn(getResource("/projection/projectionsOverview.json"));

    Map response = client.target(apiRoot)
        .path("projections")
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .get(Map.class);

    List<Map> projections = (List<Map>) response.get("projections");
    assertThat(projections).hasSize(1);
  }

  @Test
  public void testListProjectionDefinitions() throws IOException {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    when(projectionApiCallback.definitionsFetched()).thenReturn(getResource("/projection/listProjectionDefinitions.json"));

    Map response = client.target(apiRoot)
        .path("projections")
        .path("definitions")
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .get(Map.class);

    List<Map> projections = (List<Map>) response.get("definitions");
    assertThat(projections).hasSize(1);
  }

  @Test
  public void testGetProjectionDefinition() throws IOException {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    when(projectionApiCallback.definitionFetched()).thenReturn(getResource("/projection/getProjectionDefinition.json"));

    Map response = client.target(apiRoot)
        .path("projections")
        .path("definitions")
        .path("orders")
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .get(Map.class);

    assertThat(response.get("projectionName")).isEqualTo("orders");
  }


  @Test
  public void testCreateProjectionDefinition() {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    Map<String, Object> projectionDefinition = ImmutableMap.of(
        "projectionName", "orders",
        "feedName", "order",
        "handlers", ImmutableList.of(
            ImmutableMap.of(
                "eventType", "OrderPlacedEvent",
                "functions", ImmutableList.of(
                    ImmutableMap.of(
                        "function", "set",
                        "targetSelector", "$.projection.status",
                        "rawData", "PLACED"
                    ),
                    ImmutableMap.of(
                        "function", "set",
                        "targetSelector", "$.projection.orderAmount",
                        "eventSelector", "$.event.orderAmount"
                    )
                )

            )
        )
    );

    Response response = client.target(apiRoot)
        .path("projections")
        .path("definitions")
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .post(Entity.json(projectionDefinition));

    verify(projectionApiCallback, times(1)).definitionCreated(any(ProjectionDefinition.class));
    assertThat(response.getStatusInfo().getFamily()).isEqualTo(SUCCESSFUL);
  }

  @Test
  public void testCreateOrUpdateProjectionDefinition() {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();
    Map<String, Object> projectionDefinition = ImmutableMap.of(
        "projectionName", "orders",
        "feedName", "order",
        "handlers", ImmutableList.of(
            ImmutableMap.of(
                "eventType", "OrderPlacedEvent",
                "functions", ImmutableList.of(
                    ImmutableMap.of(
                        "function", "set",
                        "targetSelector", "$.projection.status",
                        "rawData", "PLACED"
                    ),
                    ImmutableMap.of(
                        "function", "set",
                        "targetSelector", "$.projection.orderAmount",
                        "eventSelector", "$.event.orderAmount"
                    )
                )

            )
        )
    );

    Response response = client.target(apiRoot)
        .path("projections")
        .path("definitions")
        .path("orders")
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .put(Entity.json(projectionDefinition));

    verify(projectionApiCallback, times(1)).definitionUpdated(any(ProjectionDefinition.class));
    assertThat(response.getStatusInfo().getFamily()).isEqualTo(SUCCESSFUL);
  }

  @Test
  public void testDeleteProjectionDefinition() {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    Response response = client.target(apiRoot)
        .path("projections")
        .path("definitions")
        .path("orders")
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .delete();

    verify(projectionApiCallback, times(1)).definitionDeleted("orders");
    assertThat(response.getStatusInfo().getFamily()).isEqualTo(SUCCESSFUL);
  }

  @Test
  public void testListSingleProjections() throws IOException {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    when(projectionApiCallback.singleProjectionsFetched("orders", emptySet(), null, "createdAt", 0, 100))
        .thenReturn(getResource("/projection/listSingleProjections.json"));

    Map response = client.target(apiRoot)
        .path("projections")
        .path("single")
        .path("orders")
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .get(Map.class);

    assertThat(response.get("totalCount")).isEqualTo(1);
    assertThat(response.get("hasMore")).isEqualTo(false);
  }

  @Test
  public void testDeleteSingleProjections() {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    Response response = client.target(apiRoot)
        .path("projections")
        .path("single")
        .path("orders")
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .delete();

    verify(projectionApiCallback, times(1)).singleProjectionsDeleted("orders");
    assertThat(response.getStatusInfo().getFamily()).isEqualTo(SUCCESSFUL);
  }

  @Test
  public void testGetSingleProjection() throws IOException {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    when(projectionApiCallback.singleProjectionFetched("orders", "84e3565e-cd61-44e7-9769-c4663588c4dd")).thenReturn(getResource("/projection/getSingleProjection.json"));

    Map response = client.target(apiRoot)
        .path("projections")
        .path("single")
        .path("orders")
        .path("84e3565e-cd61-44e7-9769-c4663588c4dd")
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .get(Map.class);

    verify(projectionApiCallback, times(1)).singleProjectionFetched("orders", "84e3565e-cd61-44e7-9769-c4663588c4dd");
    assertThat(response.get("projectionId")).isEqualTo("84e3565e-cd61-44e7-9769-c4663588c4dd");
  }

  @Test
  public void testGetAggregatedProjection() throws IOException {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    when(projectionApiCallback.aggregatedProjectionFetched("order-totals")).thenReturn(getResource("/projection/getAggregatedProjection.json"));

    Map response = client.target(apiRoot)
        .path("projections")
        .path("aggregated")
        .path("order-totals")
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .get(Map.class);

    verify(projectionApiCallback, times(1)).aggregatedProjectionFetched("order-totals");
    assertThat(response.get("projectionId")).isEqualTo("order-totals");
  }

  @Test
  public void testDeleteAggregatedProjections() {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    Response response = client.target(apiRoot)
        .path("projections")
        .path("aggregated")
        .path("order-totals")
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .delete();

    verify(projectionApiCallback, times(1)).aggregatedProjectionsDeleted("order-totals");
    assertThat(response.getStatusInfo().getFamily()).isEqualTo(SUCCESSFUL);
  }

  @Test
  public void testCreateTenant() {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    Map<String, Object> tenant = ImmutableMap.of(
        "tenantId", "e9ef574f-4563-4d56-ad9e-0a2d5ce42004",
        "reference", "Acme Inc"
    );

    Response response = client.target(apiRoot)
        .path("tenants")
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .post(Entity.json(tenant));

    verify(tenantApiCallback, times(1)).tenantAdded(any(Tenant.class));
    assertThat(response.getStatusInfo().getFamily()).isEqualTo(SUCCESSFUL);
  }

  @Test
  public void testListTenants() throws IOException {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    when(tenantApiCallback.tenantsLoaded()).thenReturn(getResource("/tenant/tenants.json"));

    Map response = client.target(apiRoot)
        .path("tenants")
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .get(Map.class);

    List<Map> feeds = (List<Map>) response.get("tenants");
    assertThat(feeds).hasSize(1);
  }

  @Test
  public void testDeleteTenant() {

    UUID aggregateId = UUID.fromString("e9ef574f-4563-4d56-ad9e-0a2d5ce42004");
    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    Response response = client.target(apiRoot)
        .path("tenants")
        .path(aggregateId.toString())
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .delete();

    verify(tenantApiCallback, times(1)).tenantDeleted(aggregateId);
    assertThat(response.getStatusInfo().getFamily()).isEqualTo(SUCCESSFUL);
  }

  private String getResource(String resource) throws IOException {
    return IOUtils.toString(getClass().getResourceAsStream(resource), UTF_8);
  }

}
