package io.serialized.client.aggregate;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class AggregateTypeRule {

  public enum Type {
    UNIQUENESS
  }

  private Type type;
  private String eventType;
  private final Set<String> fields = new LinkedHashSet<>();

  public static Builder rule(Type type) {
    return new Builder(type);
  }

  public static Builder newRule(Type type, String eventType, String... fields) {
    Builder builder = new Builder(type).withEventType(eventType);
    for (String field : fields) {
      builder.addField(field);
    }
    return builder;
  }

  public Type type() {
    return type;
  }

  public String eventType() {
    return eventType;
  }

  public Set<String> fields() {
    return unmodifiableSet(fields);
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

    private final Type type;
    private final Set<String> fields = new LinkedHashSet<>();
    private String eventType;

    public Builder(Type type) {
      this.type = type;
    }

    public Builder withEventType(String eventType) {
      this.eventType = eventType;
      return this;
    }

    public Builder addField(String field) {
      this.fields.add(field);
      return this;
    }

    public AggregateTypeRule build() {
      Validate.notEmpty(eventType, "'eventType' must be set");
      Validate.notEmpty(fields, "At least one 'field' must be specified");

      AggregateTypeRule aggregateTypeRule = new AggregateTypeRule();
      aggregateTypeRule.type = this.type;
      aggregateTypeRule.eventType = this.eventType;
      aggregateTypeRule.fields.addAll(this.fields);
      return aggregateTypeRule;
    }

  }

}
