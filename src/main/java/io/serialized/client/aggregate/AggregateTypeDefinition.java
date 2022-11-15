package io.serialized.client.aggregate;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class AggregateTypeDefinition {

  private String aggregateType;
  private String description;
  private List<AggregateTypeRule> rules;

  public String aggregateType() {
    return aggregateType;
  }

  public String description() {
    return description;
  }

  public List<AggregateTypeRule> rules() {
    return unmodifiableList(rules);
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

  public static DefinitionBuilder newAggregateTypeDefinition(String aggregateType) {
    return new DefinitionBuilder(aggregateType);
  }

  public static class DefinitionBuilder {
    private final String aggregateType;
    private final List<AggregateTypeRule> rules = new ArrayList<>();

    private String description;

    DefinitionBuilder(String aggregateType) {
      this.aggregateType = aggregateType;
    }

    /**
     * @param description Optional description
     */
    public DefinitionBuilder description(String description) {
      this.description = description;
      return this;
    }

    public DefinitionBuilder withRule(AggregateTypeRule rule) {
      this.rules.add(rule);
      return this;
    }

    public AggregateTypeDefinition build() {
      AggregateTypeDefinition definition = new AggregateTypeDefinition();
      definition.aggregateType = aggregateType;
      definition.description = description;
      definition.rules = rules;
      return definition;
    }

  }

}
