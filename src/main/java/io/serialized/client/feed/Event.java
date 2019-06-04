package io.serialized.client.feed;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Map;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class Event {

  private String eventId;
  private String eventType;
  private Map<String, Object> data;
  private String encryptedData;

  public String eventId() {
    return eventId;
  }

  public String eventType() {
    return eventType;
  }

  public Map<String, Object> data() {
    return data;
  }

  public String encryptedData() {
    return encryptedData;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
  }

}