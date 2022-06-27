package io.serialized.client.reaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class ReactionDefinition {

  private String reactionName;
  private String feedName;
  private String description;
  private String reactOnEventType;
  private String triggerTimeField;
  private String offset;
  private Set<String> cancelOnEventTypes;
  private Action action;

  public String reactionName() {
    return reactionName;
  }

  public String feedName() {
    return feedName;
  }

  public String description() {
    return description;
  }

  public String reactOnEventType() {
    return reactOnEventType;
  }

  public String triggerTimeField() {
    return triggerTimeField;
  }

  public String offset() {
    return offset;
  }

  public Set<String> cancelOnEventTypes() {
    return cancelOnEventTypes == null ? emptySet() : unmodifiableSet(cancelOnEventTypes);
  }

  public Action action() {
    return action;
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
  }

  public static DefinitionBuilder newReactionDefinition(String reactionName) {
    return new DefinitionBuilder(reactionName);
  }

  public static class DefinitionBuilder {
    private final String reactionName;
    private final Set<String> cancelOnEventTypes = new LinkedHashSet<>();

    private String feedName;
    private String description;
    private String reactOnEventType;
    private String triggerTimeField;
    private String offset;
    private Action action;

    DefinitionBuilder(String reactionName) {
      this.reactionName = reactionName;
    }

    /**
     * @param feedName Name of the feed to subscribe to.
     */
    public DefinitionBuilder feed(String feedName) {
      this.feedName = feedName;
      return this;
    }

    /**
     * @param description Optional description
     */
    public DefinitionBuilder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * @param reactOnEventType Event type to react on.
     */
    public DefinitionBuilder reactOnEventType(String reactOnEventType) {
      this.reactOnEventType = reactOnEventType;
      return this;
    }

    /**
     * @param triggerTimeField Path to event data field containing trigger time.
     *                         If not specified, trigger time will be ASAP. Dot notation supported.
     */
    public DefinitionBuilder triggerTimeField(String triggerTimeField) {
      this.triggerTimeField = triggerTimeField;
      return this;
    }

    /**
     * <pre>
     *   {@code
     *   "PT20.345S" - parses as "20.345 seconds"
     *   "PT15M"     - parses as "15 minutes" (where a minute is 60 seconds)
     *   "PT10H"     - parses as "10 hours" (where an hour is 3600 seconds)
     *   "P2D"       - parses as "2 days" (where a day is 24 hours or 86400 seconds)
     *   "P2DT3H4M"  - parses as "2 days, 3 hours and 4 minutes"
     *   "PT-6H3M"   - parses as "-6 hours and +3 minutes"
     *   "-PT6H3M"   - parses as "-6 hours and -3 minutes"
     *   "-PT-6H+3M" - parses as "+6 hours and -3 minutes"
     *   }
     * </pre>
     *
     * @param offset Trigger time offset. Defined in the ISO-8601 duration format (PnDTnHnMn.nS). May be negative.
     */
    public DefinitionBuilder offset(String offset) {
      this.offset = offset;
      return this;
    }

    /**
     * @param eventTypes Event types to cancel reaction scheduled in the future.
     */
    public DefinitionBuilder cancelOnEventType(String... eventTypes) {
      this.cancelOnEventTypes.addAll(Arrays.asList(eventTypes));
      return this;
    }

    /**
     * @param action Action to invoke when triggered.
     */
    public DefinitionBuilder action(Action action) {
      this.action = action;
      return this;
    }

    public ReactionDefinition build() {
      ReactionDefinition definition = new ReactionDefinition();
      definition.reactionName = reactionName;
      definition.feedName = feedName;
      definition.description = description;
      definition.reactOnEventType = reactOnEventType;
      definition.triggerTimeField = triggerTimeField;
      definition.offset = offset;
      definition.cancelOnEventTypes = cancelOnEventTypes;
      definition.action = action;
      return definition;
    }

  }

  public static class Action {

    private String actionType;
    private Map<String, Object> httpHeaders;
    private URI targetUri;
    private String body;
    private String signingSecret;
    private Map<String, String> valueMap;

    public String actionType() {
      return actionType;
    }

    public Map<String, Object> httpHeaders() {
      return httpHeaders;
    }

    public URI targetUri() {
      return targetUri;
    }

    public String body() {
      return body;
    }

    public String signingSecret() {
      return signingSecret;
    }

    public Map<String, String> valueMap() {
      return valueMap;
    }

    static Action newAction(String actionType, URI targetUri, Map<String, Object> httpHeaders, String signingSecret, String body, Map<String, String> valueMap) {
      Action action = new Action();
      action.actionType = actionType;
      action.targetUri = targetUri;
      action.httpHeaders = httpHeaders;
      action.body = body;
      action.signingSecret = signingSecret;
      action.valueMap = valueMap;
      return action;
    }
  }

}
