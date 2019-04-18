package io.serialized.client.projection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public class ProjectionDefinitions {

  private List<ProjectionDefinition> definitions;

  public static ProjectionDefinitions newDefinitionList(Collection<ProjectionDefinition> definitions) {
    ProjectionDefinitions projectionDefinitions = new ProjectionDefinitions();
    projectionDefinitions.definitions = new ArrayList<>(definitions);
    return projectionDefinitions;
  }

  public List<ProjectionDefinition> getDefinitions() {
    return unmodifiableList(definitions);
  }
}
