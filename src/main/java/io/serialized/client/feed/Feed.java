package io.serialized.client.feed;

public class Feed {

  private String aggregateType;
  private long aggregateCount;
  private long batchCount;
  private long eventCount;

  public String aggregateType() {
    return aggregateType;
  }

  public long aggregateCount() {
    return aggregateCount;
  }

  public long batchCount() {
    return batchCount;
  }

  public long eventCount() {
    return eventCount;
  }
}
