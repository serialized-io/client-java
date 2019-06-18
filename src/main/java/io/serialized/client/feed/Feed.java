package io.serialized.client.feed;

import org.apache.commons.lang3.builder.ToStringBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class Feed {

  private String aggregateType;
  private long aggregateCount;
  private long batchCount;
  private long eventCount;

  /**
   * @return Aggregate type producing the feed.
   */
  public String aggregateType() {
    return aggregateType;
  }

  /**
   * @return Total number of aggregates (i.e unique aggregate ID:s.
   */
  public long aggregateCount() {
    return aggregateCount;
  }

  /**
   * @return Total number of event batches
   */
  public long batchCount() {
    return batchCount;
  }

  /**
   * @return Total number of events
   */
  public long eventCount() {
    return eventCount;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
  }

}
