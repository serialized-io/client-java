package io.serialized.client.projection;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class ProjectionDefinitions {

  private List<ProjectionDefinition> definitions;

  public static ProjectionDefinitions newDefinitionList(Collection<ProjectionDefinition> definitions) {
    ProjectionDefinitions projectionDefinitions = new ProjectionDefinitions();
    projectionDefinitions.definitions = new ArrayList<>(definitions);
    return projectionDefinitions;
  }

  public List<ProjectionDefinition> definitions() {
    return definitions == null ? emptyList() : unmodifiableList(definitions);
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

}
