package io.serialized.client.feed;

public class Event {

  private String eventId;
  private String eventType;
  private Object data;

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