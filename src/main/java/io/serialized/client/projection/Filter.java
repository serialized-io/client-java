package io.serialized.client.projection;

public class Filter {

  private String filter;

  public Filter(String filter) {
    this.filter = filter;
  }

  public static Filter filter(String filter) {
    return new Filter(filter);
  }

  public String filterString() {
    return filter;
  }
}
