package io.serialized.client.reaction;

import java.util.UUID;

import static io.serialized.client.reaction.ReactionRequests.Type.SCHEDULED;
import static io.serialized.client.reaction.ReactionRequests.Type.TRIGGERED;

public class ReactionRequests {

  public enum Type {

    SCHEDULED("scheduled"),
    TRIGGERED("triggered");

    private final String name;

    Type(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

  }

  /**
   * Creates a request for listing scheduled, not yet triggered, reactions.
   */
  public static ReactionRequest.Builder scheduled() {
    return new ReactionRequest.Builder(SCHEDULED);
  }

  /**
   * Creates a request for listing already triggered reactions.
   */
  public static ReactionRequest.Builder triggered() {
    return new ReactionRequest.Builder(TRIGGERED);
  }

  /**
   * Creates a request for re-triggering an already triggered reaction.
   */
  public static TriggerReactionRequest.Builder reTriggerReaction(UUID reactionId) {
    return new TriggerReactionRequest.Builder(TRIGGERED).withReactionId(reactionId);
  }

  /**
   * Creates a request for triggering a scheduled reaction.
   */
  public static TriggerReactionRequest.Builder triggerReaction(UUID reactionId) {
    return new TriggerReactionRequest.Builder(SCHEDULED).withReactionId(reactionId);
  }

  /**
   * Creates a request for deleting a scheduled reaction.
   */
  public static DeleteReactionRequest.Builder deleteReaction(UUID reactionId) {
    return new DeleteReactionRequest.Builder().withReactionId(reactionId);
  }

}
