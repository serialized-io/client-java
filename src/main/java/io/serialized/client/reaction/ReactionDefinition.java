package io.serialized.client.reaction;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

public class ReactionDefinition {

  private String reactionName;
  private String feedName;
  private String reactOnEventType;
  private String triggerTimeField;
  private String offset;
  private Set<String> cancelOnEventTypes;
  private Action action;

  private static ReactionDefinition newDefinition(String reactionName, String feedName, String reactOnEventType, String triggerTimeField, String offset, Set<String> cancelOnEventTypes, Action action) {
    ReactionDefinition definition = new ReactionDefinition();
    definition.reactionName = reactionName;
    definition.feedName = feedName;
    definition.reactOnEventType = reactOnEventType;
    definition.triggerTimeField = triggerTimeField;
    definition.offset = offset;
    definition.cancelOnEventTypes = cancelOnEventTypes;
    definition.action = action;
    return definition;
  }

  public String getReactionName() {
    return reactionName;
  }

  public String getFeedName() {
    return feedName;
  }

  public String getReactOnEventType() {
    return reactOnEventType;
  }

  public String getTriggerTimeField() {
    return triggerTimeField;
  }

  public String getOffset() {
    return offset;
  }

  public Set<String> getCancelOnEventTypes() {
    return cancelOnEventTypes == null ? emptySet() : unmodifiableSet(cancelOnEventTypes);
  }

  public Action getAction() {
    return action;
  }

  public static DefinitionBuilder newReactionDefinition(String reactionName) {
    return new DefinitionBuilder(reactionName);
  }

  public static class DefinitionBuilder {
    private final String reactionName;
    private final Set<String> cancelOnEventTypes = new LinkedHashSet<>();

    private String feedName;
    private String reactOnEventType;
    private String triggerTimeField;
    private String offset;
    private Action action;

    DefinitionBuilder(String reactionName) {
      this.reactionName = reactionName;
    }

    public DefinitionBuilder feed(String feedName) {
      this.feedName = feedName;
      return this;
    }

    public DefinitionBuilder reactOnEventType(String reactOnEventType) {
      this.reactOnEventType = reactOnEventType;
      return this;
    }

    public DefinitionBuilder triggerTimeField(String triggerTimeField) {
      this.triggerTimeField = triggerTimeField;
      return this;
    }

    public DefinitionBuilder offset(String offset) {
      this.offset = offset;
      return this;
    }

    public DefinitionBuilder cancelOnEventType(String... eventTypes) {
      this.cancelOnEventTypes.addAll(Arrays.asList(eventTypes));
      return this;
    }

    public DefinitionBuilder action(Action action) {
      this.action = action;
      return this;
    }

    public ReactionDefinition build() {
      return newDefinition(reactionName, feedName, reactOnEventType, triggerTimeField, offset, cancelOnEventTypes, action);
    }

  }

  public static class Action {

    private String actionType;
    private Map<String, Object> httpHeaders;
    private URI targetUri;
    private String body;

    public String getActionType() {
      return actionType;
    }

    public Map<String, Object> getHttpHeaders() {
      return httpHeaders;
    }

    public URI getTargetUri() {
      return targetUri;
    }

    public String getBody() {
      return body;
    }

    static Action newAction(String actionType, URI targetUri, Map<String, Object> httpHeaders, String body) {
      Action action = new Action();
      action.actionType = actionType;
      action.targetUri = targetUri;
      action.httpHeaders = httpHeaders;
      action.body = body;
      return action;
    }
  }

}
