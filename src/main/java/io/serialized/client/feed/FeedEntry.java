package io.serialized.client.feed;

import java.util.List;

public class FeedEntry {

  private long sequenceNumber;
  private long timestamp;
  private String aggregateId;
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
