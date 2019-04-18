package io.serialized.client.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.testing.junit.DropwizardClientRule;
import io.serialized.client.aggregate.AggregateApiStub;
import io.serialized.client.aggregate.EventBatch;
import io.serialized.client.feed.FeedApiStub;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class JerseyClientIT {

  private AggregateApiStub.AggregateApiCallback aggregateApiCallback = mock(AggregateApiStub.AggregateApiCallback.class);
  private FeedApiStub.FeedApiCallback feedApiCallback = mock(FeedApiStub.FeedApiCallback.class);

  @Rule
  public final DropwizardClientRule dropwizardRule = new DropwizardClientRule(
      new AggregateApiStub(aggregateApiCallback),
      new FeedApiStub(feedApiCallback));

  @Before
  public void setUp() {
    dropwizardRule.getObjectMapper().setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
  }

  @Test
  public void testLoadAggregate() {

    when(aggregateApiCallback.aggregateLoaded("order", "99415be8-6819-4470-860c-c2933558d8d3")).thenReturn(ImmutableMap.of("apa", "banan"));

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

    verify(aggregateApiCallback, times(1)).aggregateChecked("order", "99415be8-6819-4470-860c-c2933558d8d3");
    assertThat(response.getStatusInfo().getFamily(), is(SUCCESSFUL));
  }

  @Test
  public void testDeleteAggregate() {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    String expectedToken = UUID.randomUUID().toString();
    when(aggregateApiCallback.aggregateDeleteRequested("order", "99415be8-6819-4470-860c-c2933558d8d3")).thenReturn(ImmutableMap.of("deleteToken", expectedToken));

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

    verify(aggregateApiCallback, times(1)).aggregateDeletePerformed("order", "99415be8-6819-4470-860c-c2933558d8d3", expectedToken);
    assertThat(response.getStatusInfo().getFamily(), is(SUCCESSFUL));
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
    assertThat(response.getStatusInfo().getFamily(), is(SUCCESSFUL));
  }

  @Test
  public void testStoreEvents() {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    Map eventBatch = ImmutableMap.of(
        "aggregateId", "3070b6fb-f31b-4a8e-bc03-e22d38f4076e",
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

    Response response = client.target(apiRoot)
        .path("aggregates")
        .path("order")
        .path("events")
        .request(MediaType.APPLICATION_JSON_TYPE)
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .post(Entity.json(eventBatch));

    verify(aggregateApiCallback, times(1)).eventsStored(any(EventBatch.class));

    assertThat(response.getStatusInfo().getFamily(), is(SUCCESSFUL));
  }

  @Test
  public void testGetFeedOverview() throws IOException {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();

    when(feedApiCallback.feedOverviewLoaded()).thenReturn(getResource("/feed/feeds.json"));

    Response response = client.target(apiRoot)
        .path("feeds")
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .get();

    assertThat(response.getStatusInfo().getFamily(), is(SUCCESSFUL));
  }

  @Test
  public void testGetFeedEntries() throws IOException {

    UriBuilder apiRoot = UriBuilder.fromUri(dropwizardRule.baseUri()).path("api-stub");
    Client client = ClientBuilder.newClient();
    String feedName = "orders";

    when(feedApiCallback.feedEntriesLoaded(feedName)).thenReturn(getResource("/feed/feedentries.json"));

    Response response = client.target(apiRoot)
        .path("feeds")
        .path(feedName)
        .request()
        .header("Serialized-Access-Key", "<YOUR_ACCESS_KEY>")
        .header("Serialized-Secret-Access-Key", "<YOUR_SECRET_ACCESS_KEY>")
        .get();

    assertThat(response.getStatusInfo().getFamily(), is(SUCCESSFUL));
  }


  private String getResource(String resource) throws IOException {
    return IOUtils.toString(getClass().getResourceAsStream(resource), "UTF-8");
  }

}
