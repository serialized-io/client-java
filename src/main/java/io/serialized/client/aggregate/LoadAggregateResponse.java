package io.serialized.client.aggregate;

import java.util.List;

public class LoadAggregateResponse {

  private String aggregateId;
  private String aggregateType;
  private long aggregateVersion;
  private List<Event> events;

  public String aggregateId() {
    return aggregateId;
  }

  public String aggregateType() {
    return aggregateType;
  }

  public long aggregateVersion() {
    return aggregateVersion;
  }

  public List<Event> events() {
    return events;
  }

}
