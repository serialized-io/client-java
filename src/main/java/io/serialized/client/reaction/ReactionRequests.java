package io.serialized.client.reaction;

import java.util.UUID;

public class ReactionRequests {

  /**
   * Creates a request for listing reactions.
   */
  public static ListReactionsRequest.Builder listReactions() {
    return new ListReactionsRequest.Builder();
  }

  /**
   * Creates a request for executing a reaction.
   */
  public static ExecuteReactionRequest.Builder executeReaction(UUID reactionId) {
    return new ExecuteReactionRequest.Builder().withReactionId(reactionId);
  }

  /**
   * Creates a request for deleting a scheduled reaction.
   */
  public static DeleteReactionRequest.Builder deleteReaction(UUID reactionId) {
    return new DeleteReactionRequest.Builder().withReactionId(reactionId);
  }

}
