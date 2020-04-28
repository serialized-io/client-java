package io.serialized.client.projection;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class Function {

  private String function;
  private String targetSelector;
  private String targetFilter;
  private String eventSelector;
  private String eventFilter;
  private Object rawData;

  public String function() {
    return function;
  }

  public String targetSelector() {
    return targetSelector;
  }

  public String targetFilter() {
    return targetFilter;
  }

  public String eventSelector() {
    return eventSelector;
  }

  public String eventFilter() {
    return eventFilter;
  }

  public Object rawData() {
    return rawData;
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

    // TODO: Move to constructor.
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
