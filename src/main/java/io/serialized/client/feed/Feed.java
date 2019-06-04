package io.serialized.client.feed;

import org.apache.commons.lang3.builder.ToStringBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class Feed {

  private String aggregateType;
  private long aggregateCount;
  private long batchCount;
  private long eventCount;

  public String aggregateType() {
    return aggregateType;
  }

  public long aggregateCount() {
    return aggregateCount;
  }

  public long batchCount() {
    return batchCount;
  }

  public long eventCount() {
    return eventCount;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
  }

}
