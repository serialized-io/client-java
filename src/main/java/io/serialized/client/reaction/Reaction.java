package io.serialized.client.reaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.UUID;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class Reaction {

  public enum Status {
    SCHEDULED, READY, ONGOING, COMPLETED, CANCELED, FAILED
  }

  private UUID reactionId;
  private String reactionName;
  private String aggregateType;
  private UUID aggregateId;
  private UUID eventId;
  private Long createdAt;
  private Long triggerAt;
  private Long finishedAt;
  private Status status;

  public UUID reactionId() {
    return reactionId;
  }

  public String reactionName() {
    return reactionName;
  }

  public String aggregateType() {
    return aggregateType;
  }

  public UUID aggregateId() {
    return aggregateId;
  }

  public UUID eventId() {
    return eventId;
  }

  public Long createdAt() {
    return createdAt;
  }

  public Long triggerAt() {
    return triggerAt;
  }

  public Long finishedAt() {
    return finishedAt;
  }

  public Status status() {
    return status;
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
