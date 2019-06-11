package io.serialized.client.aggregate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EventBatch {

  private String aggregateId;
  private Long expectedVersion;
  private List<Event> events;

  public EventBatch() {
  }

  public EventBatch(UUID aggregateId, List<Event> events, Long expectedVersion) {
    this.aggregateId = aggregateId.toString();
    this.events = events;
    this.expectedVersion = expectedVersion;
  }

  public String getAggregateId() {
    return aggregateId;
  }

  public Long getExpectedVersion() {
    return expectedVersion;
  }

  public List<Event> getEvents() {
    return Optional.ofNullable(events).map(Collections::unmodifiableList).orElseGet(Collections::emptyList);
  }

}
