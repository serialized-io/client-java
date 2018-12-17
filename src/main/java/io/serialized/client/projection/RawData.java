package io.serialized.client.projection;

import java.util.Map;

public class RawData {

  private final Object data;

  private RawData(Object data) {
    this.data = data;
  }

  public static RawData fromString(String data) {
    return new RawData(data);
  }

  public static RawData fromMap(Map<String, Object> data) {
    return new RawData(data);
  }

  public Object value() {
    return data;
  }

}
