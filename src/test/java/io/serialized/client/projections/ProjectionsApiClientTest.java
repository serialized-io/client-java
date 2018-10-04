package io.serialized.client.projections;

import io.dropwizard.testing.junit.DropwizardClientRule;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.projection.ProjectionApiClient;
import io.serialized.client.projection.ProjectionDefinition;
import io.serialized.client.projection.ProjectionResponse;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;

import static io.serialized.client.SerializedClientConfig.serializedConfig;
import static io.serialized.client.projection.ProjectionDefinition.projectionDefinition;
import static io.serialized.client.projection.ProjectionHandler.Function.*;
import static io.serialized.client.projection.ProjectionHandler.singleFunctionHandler;
import static io.serialized.client.projection.ProjectionQuery.aggregatedProjection;
import static io.serialized.client.projection.ProjectionQuery.singleProjection;
import static io.serialized.client.projection.Selector.eventSelector;
import static io.serialized.client.projection.Selector.targetSelector;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class ProjectionsApiClientTest {

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
  public void testCreateProjection() throws IOException {

    ProjectionDefinition highScoreProjection =
        projectionDefinition("high-score")
            .feed("game")
            .withIdFIeld("winner")
            .withHandler("GameFinished",
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
  public void testCreateProjectionWithSingleHandler() throws IOException {

    ProjectionDefinition projectionDefinition =
        projectionDefinition("high-score")
            .feed("games")
            .withIdFIeld("winner")
            .addHandler(singleFunctionHandler("GameFinished", inc("wins"))).build();

    projectionsClient.createOrUpdate(projectionDefinition);

    ArgumentCaptor<CreateProjectionDefinitionRequest> captor = ArgumentCaptor.forClass(CreateProjectionDefinitionRequest.class);
    verify(apiCallback, times(1)).projectionCreated(captor.capture());

    CreateProjectionDefinitionRequest value = captor.getValue();
    assertThat(value.projectionName, is("high-score"));
    assertThat(value.feedName, is("games"));
    assertThat(value.idField, is("winner"));
  }

  @Test
  public void testSingleProjection() throws IOException {
    ProjectionResponse<OrderBalanceProjection> projection = projectionsClient.query(
        singleProjection("orders")
            .id("723ecfce-14e9-4889-98d5-a3d0ad54912f")
            .build(OrderBalanceProjection.class));

    assertThat(projection.projectionId(), is("723ecfce-14e9-4889-98d5-a3d0ad54912f"));
    assertThat(projection.updatedAt(), is(1505754083976L));
    assertThat(projection.data().orderAmount, is(12345L));
  }

  @Test
  public void testAggregatedProjection() throws IOException {
    ProjectionResponse<OrderTotalsProjection> projection = projectionsClient.query(
        aggregatedProjection("order-totals")
            .build(OrderTotalsProjection.class));

    assertThat(projection.projectionId(), is("order-totals"));
    assertThat(projection.updatedAt(), is(1505850788368L));
    assertThat(projection.data().orderAmount, is(1000L));
    assertThat(projection.data().orderCount, is(2L));
  }

}