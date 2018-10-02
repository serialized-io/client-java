package io.serialized.client.aggregates;

import java.util.List;
import java.util.Map;

public class EventBatchDto {

  public String aggregateId;
  public List<EventDto> events;

  public static class EventDto {

    public String eventId;
    public String eventType;
    public Map<String, Object> data;

  }

}