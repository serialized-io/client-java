package io.serialized.client.aggregates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

public class EventBatch {

  private String aggregateId;
  private List<Event> events;
  private Long expectedVersion;

  public EventBatch() {
  }

  private EventBatch(Builder builder) {
    this.aggregateId = builder.aggregateId.toString();
    this.events = unmodifiableList(builder.events);
    this.expectedVersion = builder.expectedVersion;
  }

  public static Builder newBatch(String aggregateId) {
    return new Builder().aggregateId(aggregateId);
  }

  public static Builder newBatch(UUID aggregateId) {
    return new Builder().aggregateId(aggregateId);
  }

  public static class Builder {

    private final List<Event> events = new ArrayList<>();
    private UUID aggregateId;
    private Long expectedVersion;

    public Builder aggregateId(UUID aggregateId) {
      this.aggregateId = aggregateId;
      return this;
    }

    public Builder withExpectedVersion(long expectedVersion) {
      this.expectedVersion = expectedVersion;
      return this;
    }

    public Builder aggregateId(String aggregateId) {
      return aggregateId(UUID.fromString(aggregateId));
    }

    public Builder addEvent(Event event) {
      this.events.add(event);
      return this;
    }

    public Builder addEvents(Collection<Event> events) {
      this.events.addAll(events);
      return this;
    }

    public Builder addEvents(Event... events) {
      this.events.addAll(asList(events));
      return this;
    }

    public EventBatch build() {
      return new EventBatch(this);
    }

  }

}
