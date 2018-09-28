package io.serialized.client.aggregates;

import java.util.List;

public class LoadAggregateResponse {

  private String aggregateId;
  private String aggregateType;
  private long aggregateVersion;
  private List<EventBatch.Event> events;

  public String aggregateId() {
    return aggregateId;
  }

  public String aggregateType() {
    return aggregateType;
  }

  public long aggregateVersion() {
    return aggregateVersion;
  }

  public List<EventBatch.Event> events() {
    return events;
  }


}
