package io.serialized.client.feed;

import io.serialized.client.aggregates.EventBatch;

import java.util.List;

public class FeedEntry {

  public long sequenceNumber;
  public long timestamp;
  public String aggregateId;
  public List<EventBatch.Event> events;

}
