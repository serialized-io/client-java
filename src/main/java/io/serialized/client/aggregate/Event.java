package io.serialized.client.aggregate;

import com.google.common.collect.ImmutableMap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class Event<T> {

  private String eventId;
  private String eventType;
  private T data;

  public String eventId() {
    return eventId;
  }

  public String eventType() {
    return eventType;
  }

  public T data() {
    return data;
  }

  public static Event.RawBuilder newEvent(String eventType) {
    return new Event.RawBuilder(eventType);
  }

  public static <T> Event.TypedBuilder<T> newEvent(Class<T> eventType) {
    return new Event.TypedBuilder<>(eventType);
  }

  public static <T> Event.TypedBuilder<T> newEvent(T data) {
    Class<T> aClass = (Class<T>) data.getClass();
    return new TypedBuilder<>(aClass).data(data);
  }

  public static class TypedBuilder<T> {

    private UUID eventId;
    private String eventType;
    private T data;

    public TypedBuilder(String eventType) {
      this.eventId = UUID.randomUUID();
      this.eventType = eventType;
    }

    public TypedBuilder(Class<T> eventType) {
      this(eventType.getSimpleName());
    }

    public Event<T> build() {
      Event<T> event = new Event<>();
      event.eventId = eventId.toString();
      event.eventType = eventType;
      event.data = data;
      return event;
    }

    public TypedBuilder<T> eventId(UUID eventId) {
      this.eventId = eventId;
      return this;
    }

    public TypedBuilder<T> data(T dataObject) {
      this.data = dataObject;
      return this;
    }

    public TypedBuilder<T> eventId(String eventId) {
      return eventId(UUID.fromString(eventId));
    }
  }

  public static class RawBuilder {

    private UUID eventId;
    private String eventType;
    private Object data = new LinkedHashMap<>();

    public RawBuilder(String eventType) {
      this.eventId = UUID.randomUUID();
      this.eventType = eventType;
    }

    public Event build() {
      Event event = new Event<>();
      event.eventId = eventId.toString();
      event.eventType = eventType;
      event.data = data;
      return event;
    }

    public RawBuilder eventId(UUID eventId) {
      this.eventId = eventId;
      return this;
    }

    public RawBuilder data(Map<String, Object> data) {
      LinkedHashMap<Object, Object> dataMap = new LinkedHashMap<>();
      dataMap.putAll(data);
      this.data = dataMap;
      return this;
    }

    public RawBuilder data(String f1, Object d1) {
      this.data = ImmutableMap.of(f1, d1);
      return this;
    }

    public RawBuilder data(String f1, Object d1, String f2, Object d2) {
      this.data = ImmutableMap.of(f1, d1, f2, d2);
      return this;
    }

    public RawBuilder data(String f1, Object d1, String f2, Object d2, String f3, Object d3) {
      this.data = ImmutableMap.of(f1, d1, f2, d2, f3, d3);
      return this;
    }

    public RawBuilder data(String f1, Object d1, String f2, Object d2, String f3, Object d3, String f4, Object d4) {
      this.data = ImmutableMap.of(f1, d1, f2, d2, f3, d3, f4, d4);
      return this;
    }

    public RawBuilder data(String f1, Object d1, String f2, Object d2, String f3, Object d3, String f4, Object d4, String f5, Object d5) {
      this.data = ImmutableMap.of(f1, d1, f2, d2, f3, d3, f4, d4, f5, d5);
      return this;
    }

    public RawBuilder eventId(String eventId) {
      return eventId(UUID.fromString(eventId));
    }
  }

}