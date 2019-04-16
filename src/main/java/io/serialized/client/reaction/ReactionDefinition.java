package io.serialized.client.reaction;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ReactionDefinition {

  private String reactionName;
  private String feedName;
  private String reactOnEventType;
  private String triggerTimeField;
  private String offset;
  private Set<String> cancelOnEventTypes;
  private Action action;

  // For serialization
  private ReactionDefinition() {
  }

  ReactionDefinition(String reactionName, String feedName, String reactOnEventType, String triggerTimeField, String offset, Set<String> cancelOnEventTypes, Action action) {
    this.reactionName = reactionName;
    this.feedName = feedName;
    this.reactOnEventType = reactOnEventType;
    this.triggerTimeField = triggerTimeField;
    this.offset = offset;
    this.cancelOnEventTypes = cancelOnEventTypes;
    this.action = action;
  }

  public String reactionName() {
    return reactionName;
  }

  public static DefinitionBuilder newDefinition(String reactionName) {
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
      return new ReactionDefinition(reactionName, feedName, reactOnEventType, triggerTimeField, offset, cancelOnEventTypes, action);
    }

  }

  public static class Action {

    private String actionType;
    private Map<String, Object> httpHeaders;
    private URI targetUri;
    private String body;

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
