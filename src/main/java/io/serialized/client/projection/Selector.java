package io.serialized.client.projection;

import static java.lang.String.format;

public class Selector {

  private final String selector;

  private Selector(String selector) {
    this.selector = selector;
  }

  public static Selector targetSelector(String fieldName) {
    return new Selector(format("$.projection.%s", fieldName));
  }

  public static Selector eventSelector(String fieldName) {
    return new Selector(format("$.event.%s", fieldName));
  }

  public String selector() {
    return selector;
  }

}
