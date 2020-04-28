package io.serialized.client.projection;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

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

  public String eventType() {
    return eventType;
  }

  public List<Function> functions() {
    return unmodifiableList(functions);
  }

  public URI functionUri() {
    return functionUri;
  }

  public String idField() {
    return idField;
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

    // TODO: Move to constructor!
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
