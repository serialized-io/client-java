package io.serialized.client.reaction;

public class ReactionRequests {

  public static ReactionRequest.Builder scheduled() {
    return new ReactionRequest.Builder("scheduled");
  }

  public static ReactionRequest.Builder triggered() {
    return new ReactionRequest.Builder("triggered");
  }

}
