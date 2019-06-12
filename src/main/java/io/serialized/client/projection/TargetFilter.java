package io.serialized.client.projection;

public class TargetFilter {

  private String filter;

  public TargetFilter(String filter) {
    this.filter = filter;
  }

  public static TargetFilter targetFilter(String filter) {
    return new TargetFilter(filter);
  }

  public String filterString() {
    return filter;
  }

}
