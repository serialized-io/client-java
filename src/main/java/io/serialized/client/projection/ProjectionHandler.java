package io.serialized.client.projection;

import org.apache.commons.lang3.Validate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public class ProjectionHandler {

  private String eventType;
  private List<Function> functions = new ArrayList<>();
  private URI functionUri;
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

  public String getEventType() {
    return eventType;
  }

  public List<Function> getFunctions() {
    return unmodifiableList(functions);
  }

  public URI getFunctionUri() {
    return functionUri;
  }

  public String getIdField() {
    return idField;
  }

  public static class Builder {

    private final String eventType;
    private final List<Function> functions = new ArrayList<>();
    private URI functionUri;
    private String idField;

    public Builder(String eventType) {
      this.eventType = eventType;
    }

    public Builder addFunction(Function function) {
      Validate.isTrue(functionUri == null, "Cannot combine 'functions' and 'functionUri'");
      this.functions.add(function);
      return this;
    }

    public Builder withFunctionUri(URI functionUri) {
      Validate.isTrue(functions.isEmpty(), "Cannot combine 'functions' and 'functionUri'");
      this.functionUri = functionUri;
      return this;
    }

    public Builder withIdField(String idField) {
      this.idField = idField;
      return this;
    }

    public ProjectionHandler build() {
      Validate.notEmpty(eventType, "'eventType' must be set");
      ProjectionHandler projectionHandler = new ProjectionHandler();
      projectionHandler.eventType = this.eventType;
      projectionHandler.functions = this.functions;
      projectionHandler.functionUri = this.functionUri;
      projectionHandler.idField = this.idField;
      return projectionHandler;
    }

  }

}
