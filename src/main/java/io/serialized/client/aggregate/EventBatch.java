package io.serialized.client.aggregate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EventBatch {

  private UUID aggregateId;
  private Integer expectedVersion;
  private List<Event<?>> events;

  public EventBatch() {
  }

  public EventBatch(List<Event<?>> events, Integer expectedVersion) {
    this.events = events;
    this.expectedVersion = expectedVersion;
  }

  public EventBatch(UUID aggregateId, List<Event<?>> events, Integer expectedVersion) {
    this.aggregateId = aggregateId;
    this.events = events;
    this.expectedVersion = expectedVersion;
  }

  public Integer expectedVersion() {
    return expectedVersion;
  }

  public UUID aggregateId() {
    return aggregateId;
  }

  public List<Event<?>> events() {
    return Optional.ofNullable(events).map(Collections::unmodifiableList).orElseGet(Collections::emptyList);
  }

  public EventBatch withAggregateId(UUID aggregateId) {
    return new EventBatch(aggregateId, this.events, this.expectedVersion);
  }

}
