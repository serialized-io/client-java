package io.serialized.client.projection;

import java.util.ArrayList;
import java.util.List;

import static io.serialized.client.projection.Selector.targetSelector;

public class ProjectionHandler {

  private String eventType;
  private List<Function> functions = new ArrayList<>();

  public static class Function {

    private String function;
    private String targetSelector;
    private String targetFilter;
    private String eventSelector;
    private String eventFilter;
    private String rawData;

    public static Builder handlerFunction(String function) {
      return new Builder(function);
    }

    public static Function inc(String fieldName) {
      return new Builder("inc").targetSelector(targetSelector(fieldName)).build();
    }

    public static Function dec(String fieldName) {
      return new Builder("dec").targetSelector(targetSelector(fieldName)).build();
    }

    public static Function push(Selector targetSelector, RawData rawData) {
      return pushBuilder().targetSelector(targetSelector).rawData(rawData).build();
    }

    public static Function push(Selector targetSelector, Selector eventSelector) {
      return pushBuilder().targetSelector(targetSelector).eventSelector(eventSelector).build();
    }

    public static Function push(Selector targetSelector, Selector eventSelector, Filter targetFilter) {
      return pushBuilder().targetSelector(targetSelector)
          .eventSelector(eventSelector)
          .targetFilter(targetFilter)
          .build();
    }

    public static Function push(Selector targetSelector, Selector eventSelector, Filter targetFilter, Filter eventFilter) {
      return pushBuilder().targetSelector(targetSelector)
          .eventSelector(eventSelector)
          .targetFilter(targetFilter)
          .eventFilter(eventFilter)
          .build();
    }

    public static Function add(Selector targetSelector, RawData rawData) {
      return addBuilder().targetSelector(targetSelector).rawData(rawData).build();
    }

    public static Function add(Selector targetSelector, Filter targetFilter, RawData rawData) {
      return addBuilder().targetSelector(targetSelector).targetFilter(targetFilter).rawData(rawData).build();
    }

    public static Function add(Selector targetSelector, Selector eventSelector) {
      return addBuilder().targetSelector(targetSelector).eventSelector(eventSelector).build();
    }

    public static Function add(Selector targetSelector, Filter targetFilter, Selector eventSelector) {
      return addBuilder().targetSelector(targetSelector)
          .eventSelector(eventSelector)
          .targetFilter(targetFilter)
          .build();
    }

    public static Function add(Selector targetSelector, Filter targetFilter, Selector eventSelector, Filter eventFilter) {
      return addBuilder().targetSelector(targetSelector)
          .eventSelector(eventSelector)
          .targetFilter(targetFilter)
          .eventFilter(eventFilter)
          .build();
    }

    public static Function subtract(Selector targetSelector, RawData rawData) {
      return subtractBuilder().targetSelector(targetSelector).rawData(rawData).build();
    }

    public static Function subtract(Selector targetSelector, Filter targetFilter, RawData rawData) {
      return subtractBuilder().targetSelector(targetSelector).targetFilter(targetFilter).rawData(rawData).build();
    }

    public static Function subtract(Selector targetSelector, Selector eventSelector) {
      return subtractBuilder().targetSelector(targetSelector).eventSelector(eventSelector).build();
    }

    public static Function subtract(Selector targetSelector, Filter targetFilter, Selector eventSelector) {
      return subtractBuilder().targetSelector(targetSelector)
          .eventSelector(eventSelector)
          .targetFilter(targetFilter)
          .build();
    }

    public static Function subtract(Selector targetSelector, Filter targetFilter, Selector eventSelector, Filter eventFilter) {
      return subtractBuilder().targetSelector(targetSelector)
          .eventSelector(eventSelector)
          .targetFilter(targetFilter)
          .eventFilter(eventFilter)
          .build();
    }

    public static Function set(Selector targetSelector, RawData rawData) {
      return setBuilder().targetSelector(targetSelector).rawData(rawData).build();
    }

    public static Function set(Selector targetSelector, Filter targetFilter, RawData rawData) {
      return setBuilder().targetSelector(targetSelector).targetFilter(targetFilter).rawData(rawData).build();
    }

    public static Function set(Selector targetSelector, Selector eventSelector) {
      return setBuilder().targetSelector(targetSelector).eventSelector(eventSelector).build();
    }

    public static Function set(Selector targetSelector, Filter targetFilter, Selector eventSelector) {
      return setBuilder().targetSelector(targetSelector).targetFilter(targetFilter).eventSelector(eventSelector).build();
    }

    public static Function set(Selector targetSelector, Filter targetFilter, Selector eventSelector, Filter eventFilter) {
      return setBuilder().targetSelector(targetSelector).targetFilter(targetFilter).eventSelector(eventSelector).eventFilter(eventFilter).build();
    }

    public static Function merge(Selector targetSelector, RawData rawData) {
      return mergeBuilder().targetSelector(targetSelector).rawData(rawData).build();
    }

    public static Function merge(Selector targetSelector, Filter targetFilter, RawData rawData) {
      return mergeBuilder().targetSelector(targetSelector).targetFilter(targetFilter).rawData(rawData).build();
    }

    public static Function merge(Selector targetSelector, Selector eventSelector) {
      return mergeBuilder().targetSelector(targetSelector).eventSelector(eventSelector).build();
    }

    public static Function merge(Selector targetSelector, Filter targetFilter, Selector eventSelector) {
      return mergeBuilder().targetSelector(targetSelector).targetFilter(targetFilter).eventSelector(eventSelector).build();
    }

    public static Function merge(Selector targetSelector, Filter targetFilter, Selector eventSelector, Filter eventFilter) {
      return mergeBuilder().targetSelector(targetSelector).targetFilter(targetFilter).eventSelector(eventSelector).eventFilter(eventFilter).build();
    }

    private static Builder pushBuilder() {
      return new Builder("push");
    }

    private static Builder addBuilder() {
      return new Builder("add");
    }

    private static Builder subtractBuilder() {
      return new Builder("subtract");
    }

    private static Builder setBuilder() {
      return new Builder("set");
    }

    private static Builder mergeBuilder() {
      return new Builder("merge");
    }

    public static Function setref(String fieldName) {
      return new Builder("setref").targetSelector(targetSelector(fieldName)).build();
    }

    public static Function clearref() {
      return new Builder("clearref").build();
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

      public Builder eventSelector(Selector eventSelector) {
        this.eventSelector = eventSelector.selector();
        return this;
      }

      public Builder targetSelector(Selector targetSelector) {
        this.targetSelector = targetSelector.selector();
        return this;
      }

      public Function build() {
        Function function = new Function();
        function.function = this.function;
        function.targetFilter = this.targetFilter;
        function.targetSelector = this.targetSelector;
        function.eventFilter = this.eventFilter;
        function.eventSelector = this.eventSelector;
        return function;
      }

      public Builder targetFilter(Filter targetFilter) {
        this.targetFilter = targetFilter.filterString();
        return this;
      }

      public Builder eventFilter(Filter eventFilter) {
        this.eventFilter = eventFilter.filterString();
        return this;
      }

      public Builder rawData(RawData rawData) {
        this.rawData = rawData.value();
        return null;
      }
    }

  }

  public static Builder handler(String eventType) {
    return new Builder(eventType);
  }

  public static ProjectionHandler handler(String eventType, Function... functions) {
    Builder builder = new Builder(eventType);
    for (Function function : functions) {
      builder.addFunction(function);
    }
    return builder.build();
  }

  public static class Builder {

    private final String eventType;
    private final List<Function> functions = new ArrayList<>();

    public Builder(String eventType) {
      this.eventType = eventType;
    }

    public Builder addFunction(Function function) {
      this.functions.add(function);
      return this;
    }

    public ProjectionHandler build() {
      ProjectionHandler projectionHandler = new ProjectionHandler();
      projectionHandler.eventType = this.eventType;
      projectionHandler.functions = this.functions;
      return projectionHandler;
    }

  }
}
