package io.serialized.client.projection;

import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

public class ProjectionDefinition {

  private String projectionName;
  private String feedName;
  private boolean aggregated;
  private String idField;
  private List<ProjectionHandler> handlers;

  public static SingleProjectionBuilder singleProjection(String projectionName) {
    return new SingleProjectionBuilder(projectionName);
  }

  public static AggregatedProjectionBuilder aggregatedProjection(String projectionName) {
    return new AggregatedProjectionBuilder(projectionName);
  }

  public String getFeedName() {
    return feedName;
  }

  public boolean isAggregated() {
    return aggregated;
  }

  public String getIdField() {
    return idField;
  }

  public List<ProjectionHandler> getHandlers() {
    return unmodifiableList(handlers);
  }

  public String getProjectionName() {
    return projectionName;
  }

  public static class AggregatedProjectionBuilder {

    private List<ProjectionHandler> handlers = new ArrayList<>();
    private final String projectionName;
    private String feedName;

    AggregatedProjectionBuilder(String projectionName) {
      this.projectionName = projectionName;
    }

    public AggregatedProjectionBuilder feed(String feedName) {
      this.feedName = feedName;
      return this;
    }

    public AggregatedProjectionBuilder addHandler(ProjectionHandler handler) {
      this.handlers.add(handler);
      return this;
    }

    public AggregatedProjectionBuilder addHandler(String eventType, Function... functions) {
      ProjectionHandler.Builder builder = ProjectionHandler.handler(eventType);
      asList(functions).forEach(builder::addFunction);
      return addHandler(builder.build());
    }

    public ProjectionDefinition build() {
      Validate.isTrue(!handlers.isEmpty(), "'handlers' must not be empty");
      Validate.notEmpty(projectionName, "'projectionName' must be set");
      Validate.notEmpty(feedName, "'feedName' must be set");

      ProjectionDefinition definition = new ProjectionDefinition();
      definition.projectionName = projectionName;
      definition.feedName = feedName;
      definition.aggregated = true;
      definition.handlers = handlers;
      return definition;
    }

  }

  public static class SingleProjectionBuilder {

    private List<ProjectionHandler> handlers = new ArrayList<>();
    private final String projectionName;
    private String feedName;
    private String idField;

    SingleProjectionBuilder(String projectionName) {
      this.projectionName = projectionName;
    }

    public SingleProjectionBuilder feed(String feedName) {
      this.feedName = feedName;
      return this;
    }

    public SingleProjectionBuilder addHandler(ProjectionHandler handler) {
      this.handlers.add(handler);
      return this;
    }

    public SingleProjectionBuilder addHandler(String eventType, Function... functions) {
      ProjectionHandler.Builder builder = ProjectionHandler.handler(eventType);
      asList(functions).forEach(builder::addFunction);
      return addHandler(builder.build());
    }

    public SingleProjectionBuilder withIdField(String idField) {
      this.idField = idField;
      return this;
    }

    public ProjectionDefinition build() {
      Validate.isTrue(!handlers.isEmpty(), "'handlers' must not be empty");
      Validate.notEmpty(projectionName, "'projectionName' must be set");
      Validate.notEmpty(feedName, "'feedName' must be set");

      ProjectionDefinition definition = new ProjectionDefinition();
      definition.projectionName = projectionName;
      definition.feedName = feedName;
      definition.aggregated = false;
      definition.idField = idField;
      definition.handlers = handlers;
      return definition;
    }

  }

}
