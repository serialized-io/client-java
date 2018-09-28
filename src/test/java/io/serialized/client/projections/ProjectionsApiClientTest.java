package io.serialized.client.projections;

import io.serialized.client.SerializedClientConfig;
import io.serialized.client.projection.ProjectionApiClient;
import io.serialized.client.projection.ProjectionResponse;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

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

  @Rule
  public MockServerRule mockServerRule = new MockServerRule(this);

  private ProjectionApiClient projectionsClient = ProjectionApiClient.projectionsApiClient(SerializedClientConfig.builder()
      .rootApiUrl("http://localhost:" + mockServerRule.getPort())
      .accessKey("aaaaa")
      .secretAccessKey("bbbbb")
      .build()).build();

  MockServerClient mockServerClient = new MockServerClient("localhost", mockServerRule.getPort());

  @Before
  public void setUp() {
    mockServerClient.reset();
  }


  @Test
  public void testSingleProjection() throws IOException {
    mockServerClient.when(HttpRequest.request("/projections/single/orders/723ecfce-14e9-4889-98d5-a3d0ad54912f")).respond(HttpResponse.response().withBody(getResource("single_projection.json")));

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
    mockServerClient.when(HttpRequest.request("/projections/aggregated/order-totals")).respond(HttpResponse.response().withBody(getResource("aggregated_projection.json")));

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

  private String getResource(String s) throws IOException {
    return IOUtils.toString(getClass().getResourceAsStream(s));
  }
}