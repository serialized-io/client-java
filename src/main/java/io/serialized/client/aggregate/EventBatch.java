package io.serialized.client.aggregate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class EventBatch {

  private Integer expectedVersion;
  private List<Event<?>> events;

  public EventBatch() {
  }

  public EventBatch(List<Event<?>> events, Integer expectedVersion) {
    this.events = events;
    this.expectedVersion = expectedVersion;
  }

  public Integer expectedVersion() {
    return expectedVersion;
  }

  public List<Event<?>> events() {
    return Optional.ofNullable(events).map(Collections::unmodifiableList).orElseGet(Collections::emptyList);
  }

}
