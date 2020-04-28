package io.serialized.client.reaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.UUID;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class Reaction {

  private UUID reactionId;
  private String reactionName;
  private String aggregateType;
  private UUID aggregateId;
  private UUID eventId;
  private Long createdAt;
  private Long triggerAt;
  private Long finishedAt;

  public UUID getReactionId() {
    return reactionId;
  }

  public String getReactionName() {
    return reactionName;
  }

  public String getAggregateType() {
    return aggregateType;
  }

  public UUID getAggregateId() {
    return aggregateId;
  }

  public UUID getEventId() {
    return eventId;
  }

  public Long getCreatedAt() {
    return createdAt;
  }

  public Long getTriggerAt() {
    return triggerAt;
  }

  public Long getFinishedAt() {
    return finishedAt;
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

}
