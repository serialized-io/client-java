package io.serialized.client.projection;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class ProjectionDefinition {

  private String projectionName;
  private String feedName;
  private String description;
  private boolean aggregated;
  private String idField;
  private String signingSecret;
  private List<ProjectionHandler> handlers;
  private List<String> indexedFields;

  public static SingleProjectionBuilder singleProjection(String projectionName) {
    return new SingleProjectionBuilder(projectionName);
  }

  public static AggregatedProjectionBuilder aggregatedProjection(String projectionName) {
    return new AggregatedProjectionBuilder(projectionName);
  }

  public String feedName() {
    return feedName;
  }

  public String description() {
    return description;
  }

  public boolean aggregated() {
    return aggregated;
  }

  public String idField() {
    return idField;
  }

  public String signingSecret() {
    return signingSecret;
  }

  public List<ProjectionHandler> handlers() {
    return unmodifiableList(handlers);
  }

  public List<String> indexedFields() {
    return indexedFields != null ? unmodifiableList(indexedFields) : null;
  }

  public String projectionName() {
    return projectionName;
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

  public static class AggregatedProjectionBuilder {

    private final List<ProjectionHandler> handlers = new ArrayList<>();
    private final String projectionName;
    private String feedName;
    private String description;
    private String signingSecret;
    private List<String> indexedFields;

    AggregatedProjectionBuilder(String projectionName) {
      this.projectionName = projectionName;
    }

    public AggregatedProjectionBuilder feed(String feedName) {
      this.feedName = feedName;
      return this;
    }

    /**
     * @param description Optional description
     */
    public AggregatedProjectionBuilder description(String description) {
      this.description = description;
      return this;
    }

    public AggregatedProjectionBuilder signingSecret(String signingSecret) {
      this.signingSecret = signingSecret;
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

    public AggregatedProjectionBuilder withIndexedFields(List<String> indexedFields) {
      this.indexedFields = indexedFields;
      return this;
    }

    public ProjectionDefinition build() {
      Validate.isTrue(!handlers.isEmpty(), "'handlers' must not be empty");
      Validate.notEmpty(projectionName, "'projectionName' must be set");
      Validate.notEmpty(feedName, "'feedName' must be set");

      ProjectionDefinition definition = new ProjectionDefinition();
      definition.projectionName = projectionName;
      definition.feedName = feedName;
      definition.description = description;
      definition.aggregated = true;
      definition.handlers = handlers;
      definition.signingSecret = signingSecret;
      definition.indexedFields = indexedFields;
      return definition;
    }

  }

  public static class SingleProjectionBuilder {

    private final List<ProjectionHandler> handlers = new ArrayList<>();
    private final String projectionName;
    private String feedName;
    private String description;
    private String idField;
    private String signingSecret;
    private List<String> indexedFields;

    SingleProjectionBuilder(String projectionName) {
      this.projectionName = projectionName;
    }

    public SingleProjectionBuilder feed(String feedName) {
      this.feedName = feedName;
      return this;
    }

    /**
     * @param description Optional description
     */
    public SingleProjectionBuilder description(String description) {
      this.description = description;
      return this;
    }

    public SingleProjectionBuilder signingSecret(String signingSecret) {
      this.signingSecret = signingSecret;
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

    public SingleProjectionBuilder addHandler(String eventType, String feedName, Function... functions) {
      ProjectionHandler.Builder builder = ProjectionHandler.handler(eventType);
      builder.withFeedName(feedName);
      asList(functions).forEach(builder::addFunction);
      return addHandler(builder.build());
    }

    public SingleProjectionBuilder withIdField(String idField) {
      this.idField = idField;
      return this;
    }

    public SingleProjectionBuilder withIndexedFields(List<String> indexedFields) {
      this.indexedFields = indexedFields;
      return this;
    }

    public ProjectionDefinition build() {
      Validate.isTrue(!handlers.isEmpty(), "'handlers' must not be empty");
      Validate.notEmpty(projectionName, "'projectionName' must be set");
      boolean feedNameOnAllHandlers = handlers.stream().noneMatch(h -> isBlank(h.feedName()));
      Validate.isTrue(feedNameOnAllHandlers || isNotBlank(feedName), "'feedName' must be set on all handlers or on the projection definition");

      ProjectionDefinition definition = new ProjectionDefinition();
      definition.projectionName = projectionName;
      definition.feedName = feedName;
      definition.description = description;
      definition.aggregated = false;
      definition.idField = idField;
      definition.handlers = handlers;
      definition.signingSecret = signingSecret;
      definition.indexedFields = indexedFields;
      return definition;
    }

  }

}
