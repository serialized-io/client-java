package io.serialized.client.feed;

import java.util.List;

public class FeedEntry {

  public long sequenceNumber;
  public long timestamp;
  public String aggregateId;
  public List<Event> events;

}
