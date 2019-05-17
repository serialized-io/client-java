package io.serialized.client.aggregate;

import org.apache.commons.lang.Validate;

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

  public String getAggregateId() {
    return aggregateId;
  }

  public List<Event> getEvents() {
    return unmodifiableList(events);
  }

  public Long getExpectedVersion() {
    return expectedVersion;
  }

  public static Builder newBatch(String aggregateId) {
    return new Builder().aggregateId(aggregateId);
  }

  public static Builder newBatch(UUID aggregateId) {
    return new Builder().aggregateId(aggregateId);
  }

  public static EventBatch newBatch(UUID aggregateId, List<Event> events) {
    return new Builder().aggregateId(aggregateId).addEvents(events).build();
  }

  public static EventBatch newBatch(String aggregateId, List<Event> events) {
    return newBatch(UUID.fromString(aggregateId), events);
  }

  private static EventBatch newBatch(String aggregateId, List<Event> events, Long expectedVersion) {
    EventBatch eventBatch = new EventBatch();
    eventBatch.aggregateId = aggregateId;
    eventBatch.events = events;
    eventBatch.expectedVersion = expectedVersion;
    return eventBatch;
  }

  public static class Builder {

    private final List<Event> events = new ArrayList<>();
    private UUID aggregateId;
    private Long expectedVersion;

    public Builder aggregateId(UUID aggregateId) {
      this.aggregateId = aggregateId;
      return this;
    }

    public Builder aggregateId(String aggregateId) {
      return aggregateId(UUID.fromString(aggregateId));
    }

    public Builder withExpectedVersion(long expectedVersion) {
      this.expectedVersion = expectedVersion;
      return this;
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
      Validate.notNull(aggregateId, "'aggregateId' must be set");
      Validate.isTrue(!events.isEmpty(), "'events' must not be empty");
      return newBatch(aggregateId.toString(), events, expectedVersion);
    }

  }

}
