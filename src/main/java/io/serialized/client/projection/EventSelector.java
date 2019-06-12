package io.serialized.client.projection;

import static java.lang.String.format;

public class EventSelector {

  private final String selector;

  private EventSelector(String selector) {
    this.selector = selector;
  }

  public static EventSelector eventSelector(String fieldName) {
    return new EventSelector(format("$.event.%s", fieldName));
  }

  public String selector() {
    return selector;
  }

}
