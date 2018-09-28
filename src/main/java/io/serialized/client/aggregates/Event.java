package io.serialized.client.aggregates;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class Event {

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

  public static class Builder {

    private UUID eventId;
    private String eventType;
    private Object data = new LinkedHashMap<>();

    public Builder(String eventType) {
      this.eventId = UUID.randomUUID();
      this.eventType = eventType;
    }

    public Event build() {
      return new Event(eventId, eventType, data);
    }

    public Builder eventId(UUID eventId) {
      this.eventId = eventId;
      return this;
    }

    public Builder data(Map<String, Object> data) {
      LinkedHashMap<Object, Object> dataMap = new LinkedHashMap<>();
      dataMap.putAll(data);
      this.data = dataMap;
      return this;
    }

    public Builder data(Object dataObject) {
      this.data = dataObject;
      return this;
    }

    public Builder eventId(String eventId) {
      return eventId(UUID.fromString(eventId));
    }
  }
}