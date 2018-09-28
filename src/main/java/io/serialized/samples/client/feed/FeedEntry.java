package io.serialized.samples.client.feed;

import io.serialized.samples.client.aggregates.EventBatch;

import java.util.List;

public class FeedEntry {

  public long sequenceNumber;
  public long timestamp;
  public String aggregateId;
  public List<EventBatch.Event> events;

}
