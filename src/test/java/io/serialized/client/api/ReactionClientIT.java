package io.serialized.client.api;

import io.dropwizard.testing.junit.DropwizardClientRule;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.reaction.ReactionApiStub;
import io.serialized.client.reaction.ReactionClient;
import io.serialized.client.reaction.ReactionDefinition;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.net.URI;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static io.serialized.client.SerializedClientConfig.serializedConfig;
import static io.serialized.client.reaction.Actions.httpAction;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class ReactionClientIT {

  private static ReactionApiStub.Callback apiCallback = mock(ReactionApiStub.Callback.class);

  @ClassRule
  public static final DropwizardClientRule DROPWIZARD = new DropwizardClientRule(new ReactionApiStub(apiCallback));

  private SerializedClientConfig config = serializedConfig()
      .rootApiUrl(DROPWIZARD.baseUri() + "/api-stub/")
      .accessKey("aaaaa")
      .secretAccessKey("bbbbb")
      .build();

  private ReactionClient reactionClient = ReactionClient.reactionClient(config).build();

  @Before
  public void setUp() {
    DROPWIZARD.getObjectMapper().configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    reset(apiCallback);
  }

  @Test
  public void testCreateReactionDefinition() {

    URI targetUri = URI.create("https://example.com");
    String reactionName = "order-notifier";
    String eventType = "OrderPlacedEvent";
    String feedName = "orders";

    ReactionDefinition orderNotifier =
        ReactionDefinition.newDefinition(reactionName)
            .feed(feedName)
            .reactOnEventType(eventType)
            .action(httpAction(targetUri).build())
            .build();

    reactionClient.createOrUpdate(orderNotifier);

    ArgumentCaptor<ReactionDefinition> captor = ArgumentCaptor.forClass(ReactionDefinition.class);
    verify(apiCallback, times(1)).reactionCreated(captor.capture());

    ReactionDefinition value = captor.getValue();
    assertThat(value.getReactionName(), is(reactionName));
    assertThat(value.getFeedName(), is(feedName));
    assertThat(value.getReactOnEventType(), is(eventType));
    assertThat(value.getAction().getTargetUri(), is(targetUri));
  }

  @Test
  public void testGetReactionDefinition() {

    URI targetUri = URI.create("https://example.com");
    String reactionName = "order-notifier";
    String feedName = "orders";
    String eventType = "OrderPlacedEvent";

    ReactionDefinition expected = ReactionDefinition.newDefinition(reactionName)
        .reactOnEventType(eventType)
        .feed(feedName)
        .action(httpAction(targetUri).build())
        .build();
    when(apiCallback.reactionFetched()).thenReturn(expected);

    ReactionDefinition definition = reactionClient.getDefinition(reactionName);

    assertThat(definition.getReactionName(), is(reactionName));
    assertThat(definition.getFeedName(), is(feedName));
    assertThat(definition.getReactOnEventType(), is(eventType));
    assertThat(definition.getAction().getTargetUri(), is(targetUri));
  }
}