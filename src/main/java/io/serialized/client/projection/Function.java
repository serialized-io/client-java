package io.serialized.client.projection;

public class Function {

  private String function;
  private String targetSelector;
  private String targetFilter;
  private String eventSelector;
  private String eventFilter;
  private Object rawData;

  public String getFunction() {
    return function;
  }

  public String getTargetSelector() {
    return targetSelector;
  }

  public String getTargetFilter() {
    return targetFilter;
  }

  public String getEventSelector() {
    return eventSelector;
  }

  public String getEventFilter() {
    return eventFilter;
  }

  public Object getRawData() {
    return rawData;
  }

  public static Function clearref() {
    return new Builder("clearref").build();
  }

  public static Function delete() {
    return new Builder("delete").build();
  }

  public static Function clear() {
    return new Builder("clear").build();
  }

  public static Builder handlerFunction(String function) {
    return new Builder(function);
  }

  public static Builder setref() {
    return new Builder("setref");
  }

  public static Builder inc() {
    return new Builder("inc");
  }

  public static Builder dec() {
    return new Builder("dec");
  }

  public static Builder push() {
    return new Builder("push");
  }

  public static Builder add() {
    return new Builder("add");
  }

  public static Builder subtract() {
    return new Builder("subtract");
  }

  public static Builder set() {
    return new Builder("set");
  }

  public static Builder merge() {
    return new Builder("merge");
  }

  public static Builder remove() {
    return new Builder("remove");
  }

  public static Builder prepend() {
    return new Builder("prepend");
  }

  public static class Builder {

    private final String function;
    private String eventSelector;
    private String targetSelector;
    private String targetFilter;
    private String eventFilter;
    private Object rawData;

    public Builder(String function) {
      this.function = function;
    }

    public Builder with(EventSelector eventSelector) {
      this.eventSelector = eventSelector.selector();
      return this;
    }

    public Builder with(TargetSelector targetSelector) {
      this.targetSelector = targetSelector.selector();
      return this;
    }

    public Builder with(TargetFilter targetFilter) {
      this.targetFilter = targetFilter.filterString();
      return this;
    }

    public Builder with(EventFilter eventFilter) {
      this.eventFilter = eventFilter.filterString();
      return this;
    }

    public Builder with(RawData rawData) {
      this.rawData = rawData.value();
      return this;
    }

    public Function build() {
      Function function = new Function();
      function.function = this.function;
      function.targetFilter = this.targetFilter;
      function.targetSelector = this.targetSelector;
      function.eventFilter = this.eventFilter;
      function.eventSelector = this.eventSelector;
      function.rawData = this.rawData;
      return function;
    }
  }

}
