package io.serialized.client.aggregates;

import java.util.*;

import static java.util.Collections.unmodifiableList;

public class EventBatch {

  public final String aggregateId;
  public final String aggregateType;
  public final List<Event> events;

  private EventBatch(BatchBuilder builder) {
    this.aggregateId = builder.aggregateId.toString();
    this.aggregateType = builder.aggregateType;
    this.events = unmodifiableList(builder.events);
  }

  public static BatchBuilder newBatch() {
    return new BatchBuilder();
  }

  public static EventBuilder newEvent() {
    return new EventBuilder();
  }

  public static class EventBuilder {
    private UUID eventId;
    private String eventType;
    private Object data = new LinkedHashMap<>();

    public Event build() {
      return new Event(eventId, eventType, data);
    }

    public EventBuilder randomEventId() {
      this.eventId = UUID.randomUUID();
      return this;
    }

    public EventBuilder eventId(UUID eventId) {
      this.eventId = eventId;
      return this;
    }

    public EventBuilder eventType(String eventType) {
      this.eventType = eventType;
      return this;
    }

    public EventBuilder eventType(Class eventType) {
      this.eventType = eventType.getSimpleName();
      return this;
    }

    public EventBuilder data(Map<String, Object> data) {
      LinkedHashMap<Object, Object> dataMap = new LinkedHashMap<>();
      dataMap.putAll(data);
      this.data = dataMap;
      return this;
    }

    public EventBuilder data(Object dataObject) {
      this.data = dataObject;
      return this;
    }
  }

  public static class BatchBuilder {
    private UUID aggregateId;
    private String aggregateType;
    private final List<Event> events = new ArrayList<>();

    public BatchBuilder randomAggregateId() {
      this.aggregateId = UUID.randomUUID();
      return this;
    }

    public BatchBuilder aggregateId(UUID aggregateId) {
      this.aggregateId = aggregateId;
      return this;
    }

    public BatchBuilder aggregateType(String aggregateType) {
      this.aggregateType = aggregateType;
      return this;
    }

    public BatchBuilder aggregateType(Class aggregateType) {
      this.aggregateType = aggregateType.getSimpleName().toLowerCase();
      return this;
    }

    public BatchBuilder addEvent(Event event) {
      this.events.add(event);
      return this;
    }

    public EventBatch build() {
      return new EventBatch(this);
    }

  }

  public static class Event {

    private String eventId;
    private String eventType;
    private Object data;

    // For deserialization
    private Event() {
    }

    private Event(UUID eventId, String eventType, Object data) {
      this.eventId = eventId.toString();
      this.eventType = eventType;
      this.data = data;
    }

    public String eventId() {
      return eventId;
    }

    public String eventType() {
      return eventType;
    }

    public Object data() {
      return data;
    }
  }

}
