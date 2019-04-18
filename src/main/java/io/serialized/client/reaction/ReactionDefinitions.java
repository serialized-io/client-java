package io.serialized.client.reaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public class ReactionDefinitions {

  private List<ReactionDefinition> definitions;

  public static ReactionDefinitions newDefinitionList(Collection<ReactionDefinition> definitions) {
    ReactionDefinitions reactionDefinitions = new ReactionDefinitions();
    reactionDefinitions.definitions = new ArrayList<>(definitions);
    return reactionDefinitions;
  }

  public List<ReactionDefinition> getDefinitions() {
    return unmodifiableList(definitions);
  }
}
