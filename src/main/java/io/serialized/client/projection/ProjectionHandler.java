package io.serialized.client.projection;

import java.util.ArrayList;
import java.util.List;

public class ProjectionHandler {

  private String eventType;
  private List<Function> functions = new ArrayList<>();
  private String idField;

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

  public static Builder newHandler(String eventType, Function... functions) {
    Builder builder = new Builder(eventType);
    for (Function function : functions) {
      builder.addFunction(function);
    }
    return builder;
  }

  public static class Builder {

    private final String eventType;
    private final List<Function> functions = new ArrayList<>();
    private String idField;

    public Builder(String eventType) {
      this.eventType = eventType;
    }

    public Builder addFunction(Function function) {
      this.functions.add(function);
      return this;
    }

    public Builder withIdField(String idField) {
      this.idField = idField;
      return this;
    }

    public ProjectionHandler build() {
      ProjectionHandler projectionHandler = new ProjectionHandler();
      projectionHandler.eventType = this.eventType;
      projectionHandler.functions = this.functions;
      projectionHandler.idField = this.idField;
      return projectionHandler;
    }

  }
}
