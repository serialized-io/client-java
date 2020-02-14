package io.serialized.client.projection;

import java.util.List;
import java.util.Map;

public class RawData {

  private final Object data;

  private RawData(Object data) {
    this.data = data;
  }

  public static RawData rawData(String data) {
    return new RawData(data);
  }

  public static RawData rawData(Boolean data) {
    return new RawData(data);
  }

  public static RawData rawData(List<Object> data) {
    return new RawData(data);
  }

  public static RawData rawData(Map<String, Object> data) {
    return new RawData(data);
  }

  public Object value() {
    return data;
  }

}
