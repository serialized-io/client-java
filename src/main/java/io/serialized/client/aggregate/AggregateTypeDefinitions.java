package io.serialized.client.aggregate;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class AggregateTypeDefinitions {

  private List<AggregateTypeDefinition> definitions;
  private int totalCount;
  private boolean hasMore;

  public static AggregateTypeDefinitions newDefinitionList(Collection<AggregateTypeDefinition> definitions) {
    AggregateTypeDefinitions reactionDefinitions = new AggregateTypeDefinitions();
    reactionDefinitions.definitions = new ArrayList<>(definitions);
    return reactionDefinitions;
  }

  public List<AggregateTypeDefinition> definitions() {
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
