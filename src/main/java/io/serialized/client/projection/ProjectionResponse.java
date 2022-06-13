package io.serialized.client.projection;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class ProjectionResponse<T> {

  private String projectionId;
  private long createdAt;
  private long updatedAt;
  private T data;

  public ProjectionResponse() {
  }

  public ProjectionResponse(String projectionId, long createdAt, long updatedAt, T data) {
    this.projectionId = projectionId;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.data = data;
  }

  public String projectionId() {
    return projectionId;
  }

  public long createdAt() {
    return createdAt;
  }

  public long updatedAt() {
    return updatedAt;
  }

  public T data() {
    return data;
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
