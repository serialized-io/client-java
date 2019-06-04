package io.serialized.client.feed;

import java.util.List;

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

}
