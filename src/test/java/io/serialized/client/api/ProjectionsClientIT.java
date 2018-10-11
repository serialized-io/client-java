package io.serialized.client.api;

import io.dropwizard.testing.junit.DropwizardClientRule;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.projection.ProjectionApiClient;
import io.serialized.client.projection.ProjectionDefinition;
import io.serialized.client.projection.ProjectionResponse;
import io.serialized.client.projection.query.ProjectionQueries;
import io.serialized.client.projections.CreateProjectionDefinitionRequest;
import io.serialized.client.projections.ProjectionApi;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static io.serialized.client.SerializedClientConfig.serializedConfig;
import static io.serialized.client.projection.ProjectionHandler.Function.*;
import static io.serialized.client.projection.ProjectionHandler.handler;
import static io.serialized.client.projection.Selector.eventSelector;
import static io.serialized.client.projection.Selector.targetSelector;
import static io.serialized.client.projection.query.ProjectionQueries.single;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class ProjectionsClientIT {

  public static class OrderBalanceProjection {
    public long orderAmount;
  }

  public static class OrderTotalsProjection {
    public long orderAmount;
    public long orderCount;
  }

  private static ProjectionApi.Callback apiCallback = mock(ProjectionApi.Callback.class);

  @ClassRule
  public static final DropwizardClientRule DROPWIZARD = new DropwizardClientRule(new ProjectionApi(apiCallback));

  private SerializedClientConfig config = serializedConfig()
      .rootApiUrl(DROPWIZARD.baseUri() + "/api-stub/")
      .accessKey("aaaaa")
      .secretAccessKey("bbbbb")
      .build();

  private ProjectionApiClient projectionsClient = ProjectionApiClient.projectionsClient(config).build();

  @Before
  public void setUp() {
    reset(apiCallback);
  }

  @Test
  public void testCreateProjection() {

    ProjectionDefinition highScoreProjection =
        ProjectionDefinition.singleProjection("high-score")
            .feed("game")
            .withIdField("winner")
            .addHandler("GameFinished",
                set(targetSelector("playerName"), eventSelector("winner")),
                inc("wins"),
                setref("wins"))
            .build();

    projectionsClient.createOrUpdate(highScoreProjection);

    ArgumentCaptor<CreateProjectionDefinitionRequest> captor = ArgumentCaptor.forClass(CreateProjectionDefinitionRequest.class);
    verify(apiCallback, times(1)).projectionCreated(captor.capture());

    CreateProjectionDefinitionRequest value = captor.getValue();
    assertThat(value.projectionName, is("high-score"));
    assertThat(value.feedName, is("game"));
    assertThat(value.idField, is("winner"));
    assertThat(value.handlers.size(), is(1));
    assertThat(value.handlers.get(0).functions.size(), is(3));

    assertThat(value.handlers.get(0).functions.get(0).function, is("set"));
    assertThat(value.handlers.get(0).functions.get(0).targetSelector, is("$.projection.playerName"));
    assertThat(value.handlers.get(0).functions.get(0).eventSelector, is("$.event.winner"));

    assertThat(value.handlers.get(0).functions.get(1).function, is("inc"));
    assertThat(value.handlers.get(0).functions.get(1).targetSelector, is("$.projection.wins"));

    assertThat(value.handlers.get(0).functions.get(2).function, is("setref"));
    assertThat(value.handlers.get(0).functions.get(2).targetSelector, is("$.projection.wins"));
  }

  @Test
  public void testCreateProjectionWithSingleHandler() {
    ProjectionDefinition projectionDefinition =
        ProjectionDefinition.singleProjection("high-score")
            .feed("games")
            .withIdField("winner")
            .addHandler(handler("GameFinished", inc("wins"))).build();

    projectionsClient.createOrUpdate(projectionDefinition);

    ArgumentCaptor<CreateProjectionDefinitionRequest> captor = ArgumentCaptor.forClass(CreateProjectionDefinitionRequest.class);
    verify(apiCallback, times(1)).projectionCreated(captor.capture());

    CreateProjectionDefinitionRequest value = captor.getValue();
    assertThat(value.projectionName, is("high-score"));
    assertThat(value.feedName, is("games"));
    assertThat(value.idField, is("winner"));
    assertThat(value.handlers.size(), is(1));

    CreateProjectionDefinitionRequest.ProjectionHandler projectionHandler = value.handlers.get(0);
    assertThat(projectionHandler.eventType, is("GameFinished"));
    assertThat(projectionHandler.functions.size(), is(1));
    CreateProjectionDefinitionRequest.ProjectionHandler.Function function = projectionHandler.functions.get(0);
    assertThat(function.function, is("inc"));
    assertThat(function.eventSelector, nullValue());
    assertThat(function.targetSelector, is("$.projection.wins"));
    assertThat(function.eventFilter, nullValue());
    assertThat(function.targetFilter, nullValue());
    assertThat(function.rawData, nullValue());
  }

  @Test
  public void testCreateAggregatedProjection() {
    ProjectionDefinition projectionDefinition =
        ProjectionDefinition.aggregatedProjection("game-count")
            .feed("games")
            .addHandler(handler("GameFinished", inc("count"))).build();

    projectionsClient.createOrUpdate(projectionDefinition);

    ArgumentCaptor<CreateProjectionDefinitionRequest> captor = ArgumentCaptor.forClass(CreateProjectionDefinitionRequest.class);
    verify(apiCallback, times(1)).projectionCreated(captor.capture());

    CreateProjectionDefinitionRequest value = captor.getValue();
    assertThat(value.projectionName, is("game-count"));
    assertThat(value.feedName, is("games"));
    assertThat(value.idField, nullValue());
    assertThat(value.handlers.size(), is(1));

    CreateProjectionDefinitionRequest.ProjectionHandler projectionHandler = value.handlers.get(0);
    assertThat(projectionHandler.eventType, is("GameFinished"));
    assertThat(projectionHandler.functions.size(), is(1));
    CreateProjectionDefinitionRequest.ProjectionHandler.Function function = projectionHandler.functions.get(0);
    assertThat(function.function, is("inc"));
    assertThat(function.eventSelector, nullValue());
    assertThat(function.targetSelector, is("$.projection.count"));
    assertThat(function.eventFilter, nullValue());
    assertThat(function.targetFilter, nullValue());
    assertThat(function.rawData, nullValue());
  }

  @Test
  public void testSingleProjection() {
    ProjectionResponse<OrderBalanceProjection> projection = projectionsClient.query(
        single("orders")
            .id("723ecfce-14e9-4889-98d5-a3d0ad54912f")
            .build(OrderBalanceProjection.class));

    assertThat(projection.projectionId, is("723ecfce-14e9-4889-98d5-a3d0ad54912f"));
    assertThat(projection.updatedAt, is(1505754083976L));
    assertThat(projection.data.orderAmount, is(12345L));
  }

  @Test
  public void testAggregatedProjection() {
    ProjectionResponse<OrderTotalsProjection> projection = projectionsClient.query(
        ProjectionQueries.aggregated("order-totals")
            .build(OrderTotalsProjection.class));

    assertThat(projection.projectionId, is("order-totals"));
    assertThat(projection.updatedAt, is(1505850788368L));
    assertThat(projection.data.orderAmount, is(1000L));
    assertThat(projection.data.orderCount, is(2L));
  }

}