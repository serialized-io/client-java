package io.serialized.client.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.google.common.collect.ImmutableSet;
import io.dropwizard.testing.junit5.DropwizardClientExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.projection.Function;
import io.serialized.client.projection.ProjectionApiStub;
import io.serialized.client.projection.ProjectionClient;
import io.serialized.client.projection.ProjectionDefinition;
import io.serialized.client.projection.ProjectionDefinitions;
import io.serialized.client.projection.ProjectionHandler;
import io.serialized.client.projection.ProjectionRequest;
import io.serialized.client.projection.ProjectionRequests;
import io.serialized.client.projection.ProjectionResponse;
import io.serialized.client.projection.ProjectionsResponse;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static io.serialized.client.SerializedClientConfig.serializedConfig;
import static io.serialized.client.projection.EventSelector.eventSelector;
import static io.serialized.client.projection.Functions.inc;
import static io.serialized.client.projection.Functions.set;
import static io.serialized.client.projection.Functions.setref;
import static io.serialized.client.projection.ProjectionDefinitions.newDefinitionList;
import static io.serialized.client.projection.ProjectionHandler.handler;
import static io.serialized.client.projection.TargetSelector.targetSelector;
import static io.serialized.client.projection.query.ProjectionQueries.aggregated;
import static io.serialized.client.projection.query.ProjectionQueries.list;
import static io.serialized.client.projection.query.ProjectionQueries.single;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
public class ProjectionClientIT {

  public static class OrderBalanceProjection {
    public long orderAmount;
  }

  public static class OrderTotalsProjection {
    public long orderAmount;
    public long orderCount;
  }

  private final ProjectionApiStub.ProjectionApiCallback apiCallback = mock(ProjectionApiStub.ProjectionApiCallback.class);

  public final DropwizardClientExtension dropwizard = new DropwizardClientExtension(new ProjectionApiStub(apiCallback));

  @BeforeEach
  void setUp() {
    dropwizard.getObjectMapper().setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
  }

  @Test
  public void testCreateDefinitionFromJson() throws IOException {

    String projection = getResource("/projection/simpleDefinition.json");

    ProjectionClient projectionClient = getProjectionClient();

    projectionClient.createDefinition(projection);

    ArgumentCaptor<ProjectionDefinition> captor = ArgumentCaptor.forClass(ProjectionDefinition.class);
    verify(apiCallback, times(1)).definitionCreated(captor.capture());
  }

  @Test
  public void testCreateCustomFunctionDefinitionFromJson() throws IOException {

    String projection = getResource("/projection/customFunctionDefinition.json");

    ProjectionClient projectionClient = getProjectionClient();

    projectionClient.createDefinition(projection);

    ArgumentCaptor<ProjectionDefinition> captor = ArgumentCaptor.forClass(ProjectionDefinition.class);
    verify(apiCallback, times(1)).definitionCreated(captor.capture());
  }

  @Test
  public void testUpdateDefinitionFromJson() throws IOException {

    String projection = getResource("/projection/simpleDefinition.json");

    ProjectionClient projectionClient = getProjectionClient();

    projectionClient.createOrUpdate(projection);

    ArgumentCaptor<ProjectionDefinition> captor = ArgumentCaptor.forClass(ProjectionDefinition.class);
    verify(apiCallback, times(1)).definitionUpdated(captor.capture());
  }

  @Test
  public void testCreateProjectionDefinition() {

    ProjectionClient projectionClient = getProjectionClient();

    ProjectionDefinition highScoreProjection =
        ProjectionDefinition.singleProjection("high-score")
            .feed("game")
            .withIdField("winner")
            .addHandler("GameFinished",
                set().with(targetSelector("playerName")).with(eventSelector("winner")).build(),
                inc().with(targetSelector("wins")).build(),
                setref().with(targetSelector("wins")).build())
            .build();

    projectionClient.createDefinition(highScoreProjection);

    ArgumentCaptor<ProjectionDefinition> captor = ArgumentCaptor.forClass(ProjectionDefinition.class);
    verify(apiCallback, times(1)).definitionCreated(captor.capture());

    ProjectionDefinition value = captor.getValue();
    assertThat(value.projectionName()).isEqualTo("high-score");
    assertThat(value.feedName()).isEqualTo("game");
    assertThat(value.idField()).isEqualTo("winner");
    assertThat(value.handlers()).hasSize(1);
    assertThat(value.handlers().get(0).functions()).hasSize(3);

    assertThat(value.handlers().get(0).functions().get(0).function()).isEqualTo("set");
    assertThat(value.handlers().get(0).functions().get(0).targetSelector()).isEqualTo("$.projection.playerName");
    assertThat(value.handlers().get(0).functions().get(0).eventSelector()).isEqualTo("$.event.winner");

    assertThat(value.handlers().get(0).functions().get(1).function()).isEqualTo("inc");
    assertThat(value.handlers().get(0).functions().get(1).targetSelector()).isEqualTo("$.projection.wins");

    assertThat(value.handlers().get(0).functions().get(2).function()).isEqualTo("setref");
    assertThat(value.handlers().get(0).functions().get(2).targetSelector()).isEqualTo("$.projection.wins");
  }

  @Test
  public void testUpdateProjectionDefinition() {

    ProjectionClient projectionClient = getProjectionClient();

    ProjectionDefinition highScoreProjection =
        ProjectionDefinition.singleProjection("high-score")
            .feed("game")
            .withIdField("winner")
            .addHandler("GameFinished",
                set().with(targetSelector("playerName")).with(eventSelector("winner")).build(),
                inc().with(targetSelector("wins")).build(),
                setref().with(targetSelector("wins")).build())
            .build();

    projectionClient.createOrUpdate(highScoreProjection);

    ArgumentCaptor<ProjectionDefinition> captor = ArgumentCaptor.forClass(ProjectionDefinition.class);
    verify(apiCallback, times(1)).definitionUpdated(captor.capture());

    ProjectionDefinition value = captor.getValue();
    assertThat(value.projectionName()).isEqualTo("high-score");
    assertThat(value.feedName()).isEqualTo("game");
    assertThat(value.idField()).isEqualTo("winner");
    assertThat(value.handlers()).hasSize(1);
    assertThat(value.handlers().get(0).functions()).hasSize(3);

    assertThat(value.handlers().get(0).functions().get(0).function()).isEqualTo("set");
    assertThat(value.handlers().get(0).functions().get(0).targetSelector()).isEqualTo("$.projection.playerName");
    assertThat(value.handlers().get(0).functions().get(0).eventSelector()).isEqualTo("$.event.winner");

    assertThat(value.handlers().get(0).functions().get(1).function()).isEqualTo("inc");
    assertThat(value.handlers().get(0).functions().get(1).targetSelector()).isEqualTo("$.projection.wins");

    assertThat(value.handlers().get(0).functions().get(2).function()).isEqualTo("setref");
    assertThat(value.handlers().get(0).functions().get(2).targetSelector()).isEqualTo("$.projection.wins");
  }

  @Test
  public void testCreateProjectionWithSingleHandler() {

    ProjectionClient projectionClient = getProjectionClient();

    ProjectionDefinition projectionDefinition =
        ProjectionDefinition.singleProjection("high-score")
            .feed("games")
            .withIdField("winner")
            .addHandler(handler("GameFinished", inc().with(targetSelector("wins")).build())).build();

    projectionClient.createOrUpdate(projectionDefinition);

    ArgumentCaptor<ProjectionDefinition> captor = ArgumentCaptor.forClass(ProjectionDefinition.class);
    verify(apiCallback, times(1)).definitionUpdated(captor.capture());

    ProjectionDefinition value = captor.getValue();
    assertThat(value.projectionName()).isEqualTo("high-score");
    assertThat(value.feedName()).isEqualTo("games");
    assertThat(value.idField()).isEqualTo("winner");
    assertThat(value.handlers()).hasSize(1);

    ProjectionHandler projectionHandler = value.handlers().get(0);
    assertThat(projectionHandler.eventType()).isEqualTo("GameFinished");
    assertThat(projectionHandler.functions()).hasSize(1);
    Function function = projectionHandler.functions().get(0);
    assertThat(function.function()).isEqualTo("inc");
    assertThat(function.eventSelector()).isNull();
    assertThat(function.targetSelector()).isEqualTo("$.projection.wins");
    assertThat(function.eventFilter()).isNull();
    assertThat(function.targetFilter()).isNull();
    assertThat(function.rawData()).isNull();
  }

  @Test
  public void testCreateAggregatedProjectionDefinition() {

    ProjectionClient projectionClient = getProjectionClient();

    ProjectionDefinition projectionDefinition =
        ProjectionDefinition.aggregatedProjection("game-count")
            .feed("games")
            .addHandler(handler("GameFinished", inc().with(targetSelector("count")).build())).build();

    projectionClient.createOrUpdate(projectionDefinition);

    ArgumentCaptor<ProjectionDefinition> captor = ArgumentCaptor.forClass(ProjectionDefinition.class);
    verify(apiCallback, times(1)).definitionUpdated(captor.capture());

    ProjectionDefinition value = captor.getValue();
    assertThat(value.projectionName()).isEqualTo("game-count");
    assertThat(value.feedName()).isEqualTo("games");
    assertThat(value.idField()).isNull();
    assertThat(value.handlers()).hasSize(1);

    ProjectionHandler projectionHandler = value.handlers().get(0);
    assertThat(projectionHandler.eventType()).isEqualTo("GameFinished");
    assertThat(projectionHandler.functions()).hasSize(1);
    Function function = projectionHandler.functions().get(0);
    assertThat(function.function()).isEqualTo("inc");
    assertThat(function.eventSelector()).isNull();
    assertThat(function.targetSelector()).isEqualTo("$.projection.count");
    assertThat(function.eventFilter()).isNull();
    assertThat(function.targetFilter()).isNull();
    assertThat(function.rawData()).isNull();
  }

  @Test
  public void testDeleteDefinition() {
    ProjectionClient projectionClient = getProjectionClient();

    String projectionName = "orders";
    projectionClient.deleteDefinition(projectionName);
    verify(apiCallback, times(1)).definitionDeleted(projectionName);
  }

  @Test
  public void testDeleteSingleProjections() {
    ProjectionClient projectionClient = getProjectionClient();

    String projectionName = "payments";
    projectionClient.delete(ProjectionRequests.single(projectionName).build());
    verify(apiCallback, times(1)).singleProjectionsDeleted(projectionName);
  }

  @Test
  public void testDeleteAggregatedProjection() {
    ProjectionClient projectionClient = getProjectionClient();

    String projectionName = "payments-per-month";
    projectionClient.delete(ProjectionRequests.aggregated(projectionName).build());
    verify(apiCallback, times(1)).aggregatedProjectionsDeleted(projectionName);
  }

  @Test
  public void testGetDefinition() {
    ProjectionClient projectionClient = getProjectionClient();

    String projectionName = "game-count";
    String feedName = "games";
    ProjectionDefinition expected =
        ProjectionDefinition.aggregatedProjection(projectionName)
            .feed(feedName)
            .addHandler(handler("GameFinished", inc().with(targetSelector("count")).build())).build();

    when(apiCallback.definitionFetched()).thenReturn(expected);

    ProjectionDefinition definition = projectionClient.getDefinition(projectionName);
    assertThat(definition.projectionName()).isEqualTo(projectionName);
    assertThat(definition.feedName()).isEqualTo(feedName);
    assertThat(definition.handlers()).hasSize(1);
  }

  @Test
  public void testListDefinitions() {

    ProjectionClient projectionClient = getProjectionClient();

    String projectionName = "game-count";
    String feedName = "games";

    ProjectionDefinitions expected = newDefinitionList(singletonList(ProjectionDefinition.aggregatedProjection(projectionName)
        .feed(feedName)
        .addHandler(handler("GameFinished", inc().with(targetSelector("count")).build())).build()));

    when(apiCallback.definitionsFetched()).thenReturn(expected);

    ProjectionDefinitions definitions = projectionClient.listDefinitions();
    ProjectionDefinition definition = definitions.definitions().get(0);
    assertThat(definition.projectionName()).isEqualTo(projectionName);
    assertThat(definition.feedName()).isEqualTo(feedName);
    assertThat(definition.handlers()).hasSize(1);
  }

  @Test
  public void testSingleProjection() throws IOException {

    ProjectionClient projectionClient = getProjectionClient();

    String projectionName = "orders";
    String projectionId = "84e3565e-cd61-44e7-9769-c4663588c4dd";
    when(apiCallback.singleProjectionFetched(projectionName, projectionId)).thenReturn(getResource("/projection/getSingleProjection.json"));

    ProjectionResponse<OrderBalanceProjection> projection = projectionClient.query(
        single("orders")
            .withId(projectionId)
            .build(OrderBalanceProjection.class));

    assertThat(projection.projectionId()).isEqualTo(projectionId);
    assertThat(projection.createdAt()).isEqualTo(1505754083970L);
    assertThat(projection.updatedAt()).isEqualTo(1505754083976L);
    assertThat(projection.data().orderAmount).isEqualTo(12345L);
  }

  @Test
  public void testCountSingleProjection() throws IOException {

    ProjectionClient projectionClient = getProjectionClient();

    String projectionName = "orders";

    when(apiCallback.singleProjectionCount(projectionName, null)).thenReturn(getResource("/projection/getSingleProjectionCount.json"));

    ProjectionRequest request = ProjectionRequests.single("orders").build();
    long projectionCount = projectionClient.count(request);

    assertThat(projectionCount).isEqualTo(1234L);
  }

  @Test
  public void testCountSingleProjectionWithReference() throws IOException {

    ProjectionClient projectionClient = getProjectionClient();

    String projectionName = "orders";

    when(apiCallback.singleProjectionCount(projectionName, "ref")).thenReturn(getResource("/projection/getSingleProjectionCount.json"));

    ProjectionRequest request = ProjectionRequests.single("orders").withReference("ref").build();
    long projectionCount = projectionClient.count(request);

    assertThat(projectionCount).isEqualTo(1234L);
  }

  @Test
  public void testSingleProjectionForTenant() throws IOException {

    ProjectionClient projectionClient = getProjectionClient();

    String projectionName = "orders";
    String projectionId = "84e3565e-cd61-44e7-9769-c4663588c4dd";
    UUID tenantId = UUID.randomUUID();
    when(apiCallback.singleProjectionFetched(projectionName, projectionId, tenantId)).thenReturn(getResource("/projection/getSingleProjection.json"));

    ProjectionResponse<OrderBalanceProjection> projection = projectionClient.query(
        single("orders")
            .withId(projectionId)
            .withTenantId(tenantId)
            .build(OrderBalanceProjection.class));

    assertThat(projection.projectionId()).isEqualTo(projectionId);
    assertThat(projection.createdAt()).isEqualTo(1505754083970L);
    assertThat(projection.updatedAt()).isEqualTo(1505754083976L);
    assertThat(projection.data().orderAmount).isEqualTo(12345L);
  }

  @Test
  public void testListSingleProjections() throws IOException {

    ProjectionClient projectionClient = getProjectionClient();

    String projectionName = "orders";
    String reference = "externalId";
    when(apiCallback.singleProjectionsFetched(projectionName, emptySet(), reference, "-createdAt", 5, 10))
        .thenReturn(getResource("/projection/listSingleProjections.json"));

    ProjectionsResponse<Map> projections = projectionClient.query(
        list("orders").withSkip(5).withLimit(10).withSortDescending("createdAt").withReference(reference)
            .build(Map.class));

    assertThat(projections.projections()).hasSize(1);
    ProjectionResponse<Map> projectionResponse = projections.projections().iterator().next();
    assertThat(projectionResponse.projectionId()).isEqualTo("22c3780f-6dcb-440f-8532-6693be83f21c");
    assertThat(projectionResponse.createdAt()).isEqualTo(1523518143967L);
    assertThat(projectionResponse.updatedAt()).isEqualTo(1523518144467L);
    assertThat(projections.hasMore()).isEqualTo(false);
  }

  @Test
  public void testListSingleProjectionsWithIdFilter() throws IOException {

    ProjectionClient projectionClient = getProjectionClient();

    String projectionName = "orders";
    Set<String> ids = ImmutableSet.of("22c3780f-6dcb-440f-8532-6693be83f21c");

    when(apiCallback.singleProjectionsFetched(projectionName, ids, null, "createdAt", 0, 100))
        .thenReturn(getResource("/projection/listSingleProjections.json"));

    ProjectionsResponse<Map> projections = projectionClient.query(list("orders").withIds(ids).build(Map.class));

    assertThat(projections.projections()).hasSize(1);
    assertThat(projections.hasMore()).isEqualTo(false);
  }

  @Test
  public void testAggregatedProjection() throws IOException {

    ProjectionClient projectionClient = getProjectionClient();

    when(apiCallback.aggregatedProjectionFetched("order-totals")).thenReturn(getResource("/projection/getAggregatedProjection.json"));

    ProjectionResponse<OrderTotalsProjection> projection = projectionClient.query(
        aggregated("order-totals")
            .build(OrderTotalsProjection.class));

    assertThat(projection.projectionId()).isEqualTo("order-totals");
    assertThat(projection.updatedAt()).isEqualTo(1505850788368L);
    assertThat(projection.data().orderAmount).isEqualTo(1000L);
    assertThat(projection.data().orderCount).isEqualTo(2L);
  }


  private ProjectionClient getProjectionClient() {
    return ProjectionClient.projectionClient(getConfig()).build();
  }

  private SerializedClientConfig getConfig() {
    return serializedConfig()
        .rootApiUrl(dropwizard.baseUri() + "/api-stub/")
        .accessKey("aaaaa")
        .secretAccessKey("bbbbb")
        .build();
  }

  private String getResource(String resource) throws IOException {
    return IOUtils.toString(getClass().getResourceAsStream(resource), UTF_8);
  }

}
