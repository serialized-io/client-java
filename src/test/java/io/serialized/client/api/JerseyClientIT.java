package io.serialized.client.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.testing.junit.DropwizardClientRule;
import io.serialized.client.aggregate.AggregateApiStub;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.Map;
import java.util.UUID;

import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class JerseyClientIT {

  private AggregateApiStub.Callback apiCallback = mock(AggregateApiStub.Callback.class);

  @Rule
  public final DropwizardClientRule dropwizardRule = new DropwizardClientRule(new AggregateApiStub(apiCallback));

  @Before
  public void setUp() {
    dropwizardRule.getObjectMapper().setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
  }

  @Test
  public void testLoadAggregate() {

    when(apiCallback.aggregateLoaded("order", "99415be8-6819-4470-860c-c2933558d8d3")).thenReturn(ImmutableMap.of("apa", "banan"));

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    Map aggregateResponse = client.target(apiRoot)
        .path("aggregates")
        .path("order")
        .path("99415be8-6819-4470-860c-c2933558d8d3")
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .get(Map.class);

    assertThat(aggregateResponse.get("apa"), is("banan"));
  }

  @Test
  public void testCheckAggregate() {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    Response response = client.target(apiRoot)
        .path("aggregates")
        .path("order")
        .path("99415be8-6819-4470-860c-c2933558d8d3")
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .head();

    verify(apiCallback, times(1)).aggregateChecked("order", "99415be8-6819-4470-860c-c2933558d8d3");
    assertThat(response.getStatusInfo().getFamily(), is(SUCCESSFUL));
  }

  @Test
  public void testDeleteAggregate() {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    String expectedToken = UUID.randomUUID().toString();
    when(apiCallback.aggregateDeleteRequested("order", "99415be8-6819-4470-860c-c2933558d8d3")).thenReturn(ImmutableMap.of("deleteToken", expectedToken));

    Map deleteTokenResponse = client.target(apiRoot)
        .path("aggregates")
        .path("order")
        .path("99415be8-6819-4470-860c-c2933558d8d3")
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .delete(Map.class);

    String deleteToken = (String) deleteTokenResponse.get("deleteToken");

    Response response = client.target(apiRoot)
        .path("aggregates")
        .path("order")
        .path("99415be8-6819-4470-860c-c2933558d8d3")
        .queryParam("deleteToken", deleteToken)
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .delete();

    verify(apiCallback, times(1)).aggregateDeletePerformed("order", "99415be8-6819-4470-860c-c2933558d8d3", expectedToken);
    assertThat(response.getStatusInfo().getFamily(), is(SUCCESSFUL));
  }

  @Test
  public void testDeleteAggregateType() {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    String expectedToken = UUID.randomUUID().toString();
    when(apiCallback.aggregateTypeDeleteRequested("order")).thenReturn(ImmutableMap.of("deleteToken", expectedToken));

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

    verify(apiCallback, times(1)).aggregateTypeDeletePerformed("order", expectedToken);
    assertThat(response.getStatusInfo().getFamily(), is(SUCCESSFUL));
  }


}
