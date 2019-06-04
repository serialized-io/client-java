package io.serialized.client.aggregate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.unmodifiableMap;

public class Event<T> {

  private String eventId;
  private String eventType;
  private T data;
  private String encryptedData;

  public String getEventId() {
    return eventId;
  }

  public String getEventType() {
    return eventType;
  }

  public T getData() {
    return data;
  }

  public String getEncryptedData() {
    return encryptedData;
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
    private String encryptedData;

    public TypedBuilder(String eventType) {
      this.eventId = UUID.randomUUID();
      this.eventType = eventType;
    }

    public TypedBuilder(Class<T> eventType) {
      this(eventType.getSimpleName());
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

    public TypedBuilder<T> encryptedData(String data) {
      this.encryptedData = data;
      return this;
    }

    public Event<T> build() {
      Event<T> event = new Event<>();
      event.eventId = eventId.toString();
      event.eventType = eventType;
      event.data = data;
      event.encryptedData = encryptedData;
      return event;
    }

  }

  public static class RawBuilder {

    private UUID eventId;
    private String eventType;
    private Object data = new LinkedHashMap<>();
    private String encryptedData;

    public RawBuilder(String eventType) {
      this.eventId = UUID.randomUUID();
      this.eventType = eventType;
    }

    public RawBuilder eventId(UUID eventId) {
      this.eventId = eventId;
      return this;
    }

    public RawBuilder eventId(String eventId) {
      return eventId(UUID.fromString(eventId));
    }

    public RawBuilder data(Map<String, Object> data) {
      LinkedHashMap<Object, Object> dataMap = new LinkedHashMap<>();
      dataMap.putAll(data);
      this.data = dataMap;
      return this;
    }

    public RawBuilder data(String f1, Object d1) {
      Map<String, Object> map = new LinkedHashMap<>();
      map.put(f1, d1);
      this.data = unmodifiableMap(map);
      return this;
    }

    public RawBuilder data(String f1, Object d1, String f2, Object d2) {
      Map<String, Object> map = new LinkedHashMap<>();
      map.put(f1, d1);
      map.put(f2, d2);
      this.data = unmodifiableMap(map);
      return this;
    }

    public RawBuilder data(String f1, Object d1, String f2, Object d2, String f3, Object d3) {
      Map<String, Object> map = new LinkedHashMap<>();
      map.put(f1, d1);
      map.put(f2, d2);
      map.put(f3, d3);
      this.data = unmodifiableMap(map);
      return this;
    }

    public RawBuilder data(String f1, Object d1, String f2, Object d2, String f3, Object d3, String f4, Object d4) {
      Map<String, Object> map = new LinkedHashMap<>();
      map.put(f1, d1);
      map.put(f2, d2);
      map.put(f3, d3);
      map.put(f4, d4);
      this.data = unmodifiableMap(map);
      return this;
    }

    public RawBuilder data(String f1, Object d1, String f2, Object d2, String f3, Object d3, String f4, Object d4, String f5, Object d5) {
      Map<String, Object> map = new LinkedHashMap<>();
      map.put(f1, d1);
      map.put(f2, d2);
      map.put(f3, d3);
      map.put(f4, d4);
      map.put(f5, d5);
      this.data = unmodifiableMap(map);
      return this;
    }

    public RawBuilder encryptedData(String data) {
      this.encryptedData = data;
      return this;
    }

    public Event build() {
      Event event = new Event<>();
      event.eventId = eventId.toString();
      event.eventType = eventType;
      event.data = data;
      event.encryptedData = encryptedData;
      return event;
    }

  }

}
