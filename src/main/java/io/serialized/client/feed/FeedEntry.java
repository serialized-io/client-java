package io.serialized.client.feed;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class FeedEntry {

  private long sequenceNumber;
  private String aggregateId;
  private long timestamp;
  private List<Event> events;

  public long sequenceNumber() {
    return sequenceNumber;
  }

  public long timestamp() {
    return timestamp;
  }

  public String aggregateId() {
    return aggregateId;
  }

  public List<Event> events() {
    return events;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
  }

}
