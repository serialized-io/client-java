package io.serialized.client.reaction;

import java.util.UUID;

public class ReactionRequests {

  /**
   * Creates a request for listing scheduled, not yet triggered, reactions.
   */
  public static ReactionRequest.Builder scheduled() {
    return new ReactionRequest.Builder("scheduled");
  }

  /**
   * Creates a request for listing already triggered reactions.
   */
  public static ReactionRequest.Builder triggered() {
    return new ReactionRequest.Builder("triggered");
  }

  /**
   * Creates a request for re-triggering an already triggered reaction.
   */
  public static TriggerReactionRequest.Builder reTriggerReaction(UUID reactionId) {
    return new TriggerReactionRequest.Builder("triggered").withReactionId(reactionId);
  }

  /**
   * Creates a request for triggering a scheduled reaction.
   */
  public static TriggerReactionRequest.Builder triggerReaction(UUID reactionId) {
    return new TriggerReactionRequest.Builder("scheduled").withReactionId(reactionId);
  }

  /**
   * Creates a request for deleting a scheduled reaction.
   */
  public static DeleteReactionRequest.Builder deleteReaction(UUID reactionId) {
    return new DeleteReactionRequest.Builder().withReactionId(reactionId);
  }

}
