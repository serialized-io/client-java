package io.serialized.client.projection;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class ProjectionDefinition {

  private String projectionName;
  private String feedName;
  private String type;
  private String idField;
  private List<ProjectionHandler> handlers;

  // For serialization
  private ProjectionDefinition() {
  }

  private ProjectionDefinition(String projectionName, String feedName, boolean aggregated, String idField, List<ProjectionHandler> handlers) {
    this.projectionName = projectionName;
    this.feedName = feedName;
    this.type = aggregated ? "aggregated" : "single";
    this.idField = idField;
    this.handlers = handlers;
  }

  public static Builder singleProjection(String projectionName) {
    return new Builder(projectionName, false);
  }

  public String projectionName() {
    return projectionName;
  }

  public static class Builder {

    private List<ProjectionHandler> handlers = new ArrayList<>();
    private boolean aggregated;
    private final String projectionName;
    private String feedName;
    private String idField;

    public Builder(String projectionName, boolean aggregated) {
      this.projectionName = projectionName;
      this.aggregated = aggregated;
    }

    public Builder feed(String feedName) {
      this.feedName = feedName;
      return this;
    }

    public Builder addHandler(ProjectionHandler handler) {
      this.handlers.add(handler);
      return this;
    }

    public Builder withHandler(String eventType, ProjectionHandler.Function... functions) {
      ProjectionHandler.Builder builder = ProjectionHandler.newHandler(eventType);
      asList(functions).forEach(builder::addFunction);
      return addHandler(builder.build());
    }

    public Builder asAggregated() {
      this.aggregated = true;
      return this;
    }

    public Builder withIdField(String idField) {
      this.idField = idField;
      return this;
    }

    public ProjectionDefinition build() {
      return new ProjectionDefinition(projectionName, feedName, aggregated, idField, handlers);
    }
  }

}
