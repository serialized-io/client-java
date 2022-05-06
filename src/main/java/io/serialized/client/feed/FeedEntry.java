package io.serialized.client.feed;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class FeedEntry {

  private long sequenceNumber;
  private UUID aggregateId;
  private long timestamp;
  private String feedName;
  private List<Event> events;

  /**
   * @return Feed unique sequence number identifying this entry.
   */
  public long sequenceNumber() {
    return sequenceNumber;
  }

  /**
   * @return Time when the entry was created.
   */
  public long timestamp() {
    return timestamp;
  }

  /**
   * @return ID the aggregate the events in this entry belongs to.
   */
  public UUID aggregateId() {
    return aggregateId;
  }

  public String feedName() {
    return feedName;
  }

  /**
   * @return The events.
   */
  public List<Event> events() {
    return events == null ? emptyList() : unmodifiableList(events);
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
