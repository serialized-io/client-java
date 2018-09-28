package io.serialized.client.projections;

import io.dropwizard.testing.junit.DropwizardClientRule;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.projection.ProjectionApiClient;
import io.serialized.client.projection.ProjectionResponse;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;

import static io.serialized.client.projection.ProjectionQuery.aggregatedProjection;
import static io.serialized.client.projection.ProjectionQuery.singleProjection;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ProjectionsApiClientTest {

  public static class OrderBalanceProjection {
    public long orderAmount;
  }

  public static class OrderTotalsProjection {
    public long orderAmount;
    public long orderCount;
  }

  @ClassRule
  public static final DropwizardClientRule DROPWIZARD = new DropwizardClientRule(new ProjectionApi());

  private ProjectionApiClient projectionsClient = ProjectionApiClient.projectionsClient(SerializedClientConfig.builder()
      .rootApiUrl(DROPWIZARD.baseUri() + "/api-stub/")
      .accessKey("aaaaa")
      .secretAccessKey("bbbbb")
      .build()).build();

  @Test
  public void testSingleProjection() throws IOException {
    ProjectionResponse<OrderBalanceProjection> projection = projectionsClient.query(
        singleProjection("orders")
            .id("723ecfce-14e9-4889-98d5-a3d0ad54912f")
            .as(OrderBalanceProjection.class)
            .build()
    );

    assertThat(projection.projectionId(), is("723ecfce-14e9-4889-98d5-a3d0ad54912f"));
    assertThat(projection.updatedAt(), is(1505754083976L));
    assertThat(projection.data().orderAmount, is(12345L));
  }

  @Test
  public void testAggregatedProjection() throws IOException {
    ProjectionResponse<OrderTotalsProjection> projection = projectionsClient.query(
        aggregatedProjection("order-totals")
            .as(OrderTotalsProjection.class)
            .build()
    );

    assertThat(projection.projectionId(), is("order-totals"));
    assertThat(projection.updatedAt(), is(1505850788368L));
    assertThat(projection.data().orderAmount, is(1000L));
    assertThat(projection.data().orderCount, is(2L));
  }

}