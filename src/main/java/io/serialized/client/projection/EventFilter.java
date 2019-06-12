package io.serialized.client.projection;

public class EventFilter {

  private String filter;

  public EventFilter(String filter) {
    this.filter = filter;
  }

  public static EventFilter eventFilter(String filter) {
    return new EventFilter(filter);
  }

  public String filterString() {
    return filter;
  }

}
