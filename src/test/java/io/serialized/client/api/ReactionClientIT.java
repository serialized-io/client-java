package io.serialized.client.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.testing.junit5.DropwizardClientExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.reaction.Reaction;
import io.serialized.client.reaction.ReactionApiStub;
import io.serialized.client.reaction.ReactionClient;
import io.serialized.client.reaction.ReactionDefinition;
import io.serialized.client.reaction.ReactionDefinitions;
import io.serialized.client.reaction.ReactionRequest;
import io.serialized.client.reaction.ReactionRequests;
import io.serialized.client.reaction.ReactionsResponse;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.serialized.client.SerializedClientConfig.serializedConfig;
import static io.serialized.client.reaction.Actions.httpAction;
import static io.serialized.client.reaction.ReactionDefinitions.newDefinitionList;
import static io.serialized.client.reaction.ReactionRequests.deleteReaction;
import static io.serialized.client.reaction.ReactionRequests.reTriggerReaction;
import static io.serialized.client.reaction.ReactionRequests.triggerReaction;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.ZoneOffset.UTC;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
public class ReactionClientIT {

  private final ReactionApiStub.ReactionApiCallback apiCallback = mock(ReactionApiStub.ReactionApiCallback.class);

  public final DropwizardClientExtension dropwizard = new DropwizardClientExtension(new ReactionApiStub(apiCallback));

  @BeforeEach
  void setUp() {
    dropwizard.getObjectMapper().setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
  }

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
    String description = "Reaction sending notifications for new orders";

    ReactionDefinition orderNotifier =
        ReactionDefinition.newReactionDefinition(reactionName)
            .feed(feedName)
            .description(description)
            .reactOnEventType(eventType)
            .action(httpAction(targetUri).build())
            .build();

    reactionClient.createDefinition(orderNotifier);

    ArgumentCaptor<ReactionDefinition> captor = ArgumentCaptor.forClass(ReactionDefinition.class);
    verify(apiCallback, times(1)).definitionCreated(captor.capture());

    ReactionDefinition value = captor.getValue();
    assertThat(value.reactionName()).isEqualTo(reactionName);
    assertThat(value.feedName()).isEqualTo(feedName);
    assertThat(value.description()).isEqualTo(description);
    assertThat(value.reactOnEventType()).isEqualTo(eventType);
    assertThat(value.action().targetUri()).isEqualTo(targetUri);
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
    assertThat(value.reactionName()).isEqualTo(reactionName);
    assertThat(value.feedName()).isEqualTo(feedName);
    assertThat(value.reactOnEventType()).isEqualTo(eventType);
    assertThat(value.action().targetUri()).isEqualTo(targetUri);
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
    String description = "Version1";

    ReactionDefinition expected = ReactionDefinition.newReactionDefinition(reactionName)
        .reactOnEventType(eventType)
        .feed(feedName)
        .description(description)
        .action(httpAction(targetUri).build())
        .build();
    when(apiCallback.definitionFetched()).thenReturn(expected);

    ReactionDefinition definition = reactionClient.getDefinition(reactionName);

    assertThat(definition.reactionName()).isEqualTo(reactionName);
    assertThat(definition.feedName()).isEqualTo(feedName);
    assertThat(definition.description()).isEqualTo(description);
    assertThat(definition.reactOnEventType()).isEqualTo(eventType);
    assertThat(definition.action().targetUri()).isEqualTo(targetUri);
  }

  @Test
  public void testListReactionDefinitions() {

    ReactionClient reactionClient = getReactionClient();

    URI targetUri = URI.create("https://example.com");
    String reactionName = "order-notifier";
    String feedName = "orders";
    String eventType = "OrderPlacedEvent";

    ReactionDefinitions expected = newDefinitionList(singletonList(ReactionDefinition.newReactionDefinition(reactionName)
        .reactOnEventType(eventType)
        .feed(feedName)
        .action(httpAction(targetUri).build())
        .build()));

    when(apiCallback.definitionsFetched()).thenReturn(expected);

    ReactionDefinitions reactionDefinitions = reactionClient.listDefinitions();

    ReactionDefinition definition = reactionDefinitions.definitions().get(0);
    assertThat(definition.reactionName()).isEqualTo(reactionName);
    assertThat(definition.feedName()).isEqualTo(feedName);
    assertThat(definition.reactOnEventType()).isEqualTo(eventType);
    assertThat(definition.action().targetUri()).isEqualTo(targetUri);
  }

  @Test
  public void testListScheduledReactions() {

    ReactionClient reactionClient = getReactionClient();

    String aggregateId = "750fc2c9-0c2e-4504-9a95-87281d7bbd1f";
    String reactionName = "order-notifier";
    String aggregateType = "orders";

    Map<String, Object> expected = new ImmutableMap.Builder<String, Object>()
        .put("reactions", ImmutableList.of(
            ImmutableMap.of(
                "reactionId", aggregateId,
                "reactionName", reactionName,
                "aggregateType", aggregateType,
                "triggerAt", LocalDate.now().plusDays(1).atStartOfDay().toInstant(UTC).toEpochMilli()
            )
        )).build();

    ReactionRequest request = ReactionRequests.scheduled().build();

    when(apiCallback.scheduledReactionsFetched()).thenReturn(expected);

    ReactionsResponse response = reactionClient.listReactions(request);
    List<Reaction> reactions = response.reactions();
    assertThat(reactions).hasSize(1);

    Reaction reaction = reactions.iterator().next();
    assertThat(reaction.reactionId().toString()).isEqualTo(aggregateId);
    assertThat(reaction.reactionName()).isEqualTo(reactionName);
    assertThat(reaction.aggregateType()).isEqualTo(aggregateType);
    assertThat(reaction.triggerAt()).isGreaterThan(System.currentTimeMillis());
  }

  @Test
  public void testListTriggeredReactions() {

    ReactionClient reactionClient = getReactionClient();

    String aggregateId = "750fc2c9-0c2e-4504-9a95-87281d7bbd1f";
    String reactionName = "order-notifier";
    String aggregateType = "orders";

    Map<String, Object> expected = new ImmutableMap.Builder<String, Object>()
        .put("reactions", ImmutableList.of(
            ImmutableMap.of(
                "reactionId", aggregateId,
                "reactionName", reactionName,
                "aggregateType", aggregateType,
                "finishedAt", LocalDate.now().minusDays(1).atStartOfDay().toInstant(UTC)
            )
        )).build();

    ReactionRequest request = ReactionRequests.triggered().build();

    when(apiCallback.triggeredReactionsFetched()).thenReturn(expected);

    ReactionsResponse response = reactionClient.listReactions(request);
    List<Reaction> reactions = response.reactions();
    assertThat(reactions).hasSize(1);

    Reaction reaction = reactions.iterator().next();
    assertThat(reaction.reactionId().toString()).isEqualTo(aggregateId);
    assertThat(reaction.reactionName()).isEqualTo(reactionName);
    assertThat(reaction.aggregateType()).isEqualTo(aggregateType);
    assertThat(reaction.finishedAt()).isLessThan(System.currentTimeMillis());
  }

  @Test
  public void testTriggerScheduledReaction() {

    ReactionClient reactionClient = getReactionClient();

    String aggregateId = "750fc2c9-0c2e-4504-9a95-87281d7bbd1f";

    reactionClient.triggerReaction(triggerReaction(UUID.fromString(aggregateId)).build());

    verify(apiCallback).scheduledReactionTriggered(aggregateId);
  }

  @Test
  public void testReTriggerTriggeredReaction() {

    ReactionClient reactionClient = getReactionClient();

    String aggregateId = "750fc2c9-0c2e-4504-9a95-87281d7bbd1f";

    reactionClient.triggerReaction(reTriggerReaction(UUID.fromString(aggregateId)).build());

    verify(apiCallback).triggeredReactionReTriggered(aggregateId);
  }

  @Test
  public void testDeleteScheduledReaction() {

    ReactionClient reactionClient = getReactionClient();

    String aggregateId = "750fc2c9-0c2e-4504-9a95-87281d7bbd1f";

    reactionClient.deleteReaction(deleteReaction(UUID.fromString(aggregateId)).build());

    verify(apiCallback).scheduledReactionDeleted(aggregateId);
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
    return IOUtils.toString(getClass().getResourceAsStream(resource), UTF_8);
  }

}
