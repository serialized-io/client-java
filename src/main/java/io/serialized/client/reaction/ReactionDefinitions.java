package io.serialized.client.reaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class ReactionDefinitions {

  private List<ReactionDefinition> definitions;

  public static ReactionDefinitions newDefinitionList(Collection<ReactionDefinition> definitions) {
    ReactionDefinitions reactionDefinitions = new ReactionDefinitions();
    reactionDefinitions.definitions = new ArrayList<>(definitions);
    return reactionDefinitions;
  }

  public List<ReactionDefinition> definitions() {
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
