package io.serialized.client.aggregates;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.unmodifiableList;

public class EventBatch {

  private String aggregateId;
  private List<Event> events;

  public EventBatch() {
  }

  private EventBatch(Builder builder) {
    this.aggregateId = builder.aggregateId.toString();
    this.events = unmodifiableList(builder.events);
  }

  public static Builder newBatch() {
    return new Builder();
  }

  public static Event.Builder newEvent(String eventType) {
    return new Event.Builder(eventType);
  }

  public static Event.Builder newEvent(Object event) {
    String eventType = event.getClass().getSimpleName();
    return newEvent(eventType).data(event);
  }

  public static class Builder {

    private final List<Event> events = new ArrayList<>();
    private UUID aggregateId;


    public Builder aggregateId(UUID aggregateId) {
      this.aggregateId = aggregateId;
      return this;
    }

    public Builder aggregateId(String aggregateId) {
      return aggregateId(UUID.fromString(aggregateId));
    }

    public Builder addEvent(Event event) {
      this.events.add(event);
      return this;
    }

    public EventBatch build() {
      return new EventBatch(this);
    }

  }


}
