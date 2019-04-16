package io.serialized.client.reaction;

import java.net.URI;
import java.util.Set;

public class CreateReactionDefinitionRequest {

  public String reactionName;
  public String feedName;
  public String reactOnEventType;
  public String triggerTimeField;
  public String offset;
  public Set<String> cancelOnEventTypes;
  public Action action;

  public static class Action {
    public URI targetUri;
  }

}
