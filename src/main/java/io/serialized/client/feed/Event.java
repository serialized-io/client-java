package io.serialized.client.feed;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
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
    return data == null ? emptyMap() : unmodifiableMap(data);
  }

  public <T> T dataValueAs(String key, Class<T> clazz) {
    Object obj = data().get(key);
    if (obj == null) {
      throw new IllegalArgumentException("Key does not exist: " + key);
    } else {
      return clazz.cast(data.get(key));
    }
  }

  public String encryptedData() {
    return encryptedData;
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
  }

}
