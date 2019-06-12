package io.serialized.client.projection;

import static java.lang.String.format;

public class TargetSelector {

  private final String selector;

  private TargetSelector(String selector) {
    this.selector = selector;
  }

  public static TargetSelector targetSelector(String fieldName) {
    return new TargetSelector(format("$.projection.%s", fieldName));
  }

  public String selector() {
    return selector;
  }

}
