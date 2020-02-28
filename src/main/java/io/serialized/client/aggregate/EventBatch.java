package io.serialized.client.aggregate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class EventBatch {

  private Long expectedVersion;
  private List<Event<?>> events;

  public EventBatch() {
  }

  public EventBatch(List<Event<?>> events, Long expectedVersion) {
    this.events = events;
    this.expectedVersion = expectedVersion;
  }

  public Long getExpectedVersion() {
    return expectedVersion;
  }

  public List<Event<?>> getEvents() {
    return Optional.ofNullable(events).map(Collections::unmodifiableList).orElseGet(Collections::emptyList);
  }

}
