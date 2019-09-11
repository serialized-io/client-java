package io.serialized.client.api;

import io.dropwizard.testing.junit.DropwizardClientRule;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.reaction.ReactionApiStub;
import io.serialized.client.reaction.ReactionClient;
import io.serialized.client.reaction.ReactionDefinition;
import io.serialized.client.reaction.ReactionDefinitions;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.net.URI;

import static io.serialized.client.SerializedClientConfig.serializedConfig;
import static io.serialized.client.reaction.Actions.httpAction;
import static io.serialized.client.reaction.ReactionDefinitions.newDefinitionList;
import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReactionClientIT {

  private final ReactionApiStub.ReactionApiCallback apiCallback = mock(ReactionApiStub.ReactionApiCallback.class);

  @Rule
  public final DropwizardClientRule dropwizard = new DropwizardClientRule(new ReactionApiStub(apiCallback));

  @Test
  public void testCreateDefinitionFromJson() throws IOException {

    ReactionClient reactionClient = getReactionClient();

    String definition = getResource("/reaction/simpleDefinition.json");

    reactionClient.createDefinition(definition);

    ArgumentCaptor<ReactionDefinition> captor = ArgumentCaptor.forClass(ReactionDefinition.class);
    verify(apiCallback, times(1)).definitionCreated(captor.capture());

  }

  @Test
  public void testUpdateDefinitionFromJson() throws IOException {

    ReactionClient reactionClient = getReactionClient();

    String definition = getResource("/reaction/simpleDefinition.json");

    reactionClient.createOrUpdate(definition);

    ArgumentCaptor<ReactionDefinition> captor = ArgumentCaptor.forClass(ReactionDefinition.class);
    verify(apiCallback, times(1)).definitionUpdated(captor.capture());

  }

  @Test
  public void testCreateReactionDefinition() {

    ReactionClient reactionClient = getReactionClient();

    URI targetUri = URI.create("https://example.com");
    String reactionName = "order-notifier";
    String eventType = "OrderPlacedEvent";
    String feedName = "orders";

    ReactionDefinition orderNotifier =
        ReactionDefinition.newReactionDefinition(reactionName)
            .feed(feedName)
            .reactOnEventType(eventType)
            .action(httpAction(targetUri).build())
            .build();

    reactionClient.createDefinition(orderNotifier);

    ArgumentCaptor<ReactionDefinition> captor = ArgumentCaptor.forClass(ReactionDefinition.class);
    verify(apiCallback, times(1)).definitionCreated(captor.capture());

    ReactionDefinition value = captor.getValue();
    assertThat(value.getReactionName(), is(reactionName));
    assertThat(value.getFeedName(), is(feedName));
    assertThat(value.getReactOnEventType(), is(eventType));
    assertThat(value.getAction().getTargetUri(), is(targetUri));
  }

  @Test
  public void testUpdateReactionDefinition() {

    ReactionClient reactionClient = getReactionClient();

    URI targetUri = URI.create("https://example.com");
    String reactionName = "order-notifier";
    String eventType = "OrderPlacedEvent";
    String feedName = "orders";

    ReactionDefinition orderNotifier =
        ReactionDefinition.newReactionDefinition(reactionName)
            .feed(feedName)
            .reactOnEventType(eventType)
            .action(httpAction(targetUri).build())
            .build();

    reactionClient.createOrUpdate(orderNotifier);

    ArgumentCaptor<ReactionDefinition> captor = ArgumentCaptor.forClass(ReactionDefinition.class);
    verify(apiCallback, times(1)).definitionUpdated(captor.capture());

    ReactionDefinition value = captor.getValue();
    assertThat(value.getReactionName(), is(reactionName));
    assertThat(value.getFeedName(), is(feedName));
    assertThat(value.getReactOnEventType(), is(eventType));
    assertThat(value.getAction().getTargetUri(), is(targetUri));
  }

  @Test
  public void testDeleteReactionDefinition() {

    ReactionClient reactionClient = getReactionClient();

    String reactionName = "order-notifier";
    reactionClient.deleteDefinition(reactionName);
    verify(apiCallback, times(1)).definitionDeleted(reactionName);
  }

  @Test
  public void testGetReactionDefinition() {

    ReactionClient reactionClient = getReactionClient();

    URI targetUri = URI.create("https://example.com");
    String reactionName = "order-notifier";
    String feedName = "orders";
    String eventType = "OrderPlacedEvent";

    ReactionDefinition expected = ReactionDefinition.newReactionDefinition(reactionName)
        .reactOnEventType(eventType)
        .feed(feedName)
        .action(httpAction(targetUri).build())
        .build();
    when(apiCallback.definitionFetched()).thenReturn(expected);

    ReactionDefinition definition = reactionClient.getDefinition(reactionName);

    assertThat(definition.getReactionName(), is(reactionName));
    assertThat(definition.getFeedName(), is(feedName));
    assertThat(definition.getReactOnEventType(), is(eventType));
    assertThat(definition.getAction().getTargetUri(), is(targetUri));
  }

  @Test
  public void testListReactionDefinitions() {

    ReactionClient reactionClient = getReactionClient();

    URI targetUri = URI.create("https://example.com");
    String reactionName = "order-notifier";
    String feedName = "orders";
    String eventType = "OrderPlacedEvent";

    ReactionDefinitions expected = newDefinitionList(asList(ReactionDefinition.newReactionDefinition(reactionName)
        .reactOnEventType(eventType)
        .feed(feedName)
        .action(httpAction(targetUri).build())
        .build()));

    when(apiCallback.definitionsFetched()).thenReturn(expected);

    ReactionDefinitions reactionDefinitions = reactionClient.listDefinitions();

    ReactionDefinition definition = reactionDefinitions.getDefinitions().get(0);
    assertThat(definition.getReactionName(), is(reactionName));
    assertThat(definition.getFeedName(), is(feedName));
    assertThat(definition.getReactOnEventType(), is(eventType));
    assertThat(definition.getAction().getTargetUri(), is(targetUri));
  }

  private ReactionClient getReactionClient() {
    return ReactionClient.reactionClient(getConfig()).build();
  }

  private SerializedClientConfig getConfig() {
    return serializedConfig()
        .rootApiUrl(dropwizard.baseUri() + "/api-stub/")
        .accessKey("aaaaa")
        .secretAccessKey("bbbbb")
        .build();
  }

  private String getResource(String resource) throws IOException {
    return IOUtils.toString(getClass().getResourceAsStream(resource), "UTF-8");
  }

}
