package io.serialized.client.feed;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class FeedEntry {

  private long sequenceNumber;
  private String aggregateId;
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
   * @return ID the the aggregate the events in this entry belongs to.
   */
  public String aggregateId() {
    return aggregateId;
  }

  public String feedName() {
    return feedName;
  }

  /**
   * @return The events.
   */
  public List<Event> events() {
    return events;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
  }

}
