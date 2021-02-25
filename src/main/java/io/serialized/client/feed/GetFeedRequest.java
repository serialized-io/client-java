package io.serialized.client.feed;

import org.apache.commons.lang3.Validate;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.ValueRange;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static java.lang.String.format;

public class GetFeedRequest {

  public final String feedName;
  public final Integer limit;
  public final Duration waitTime;
  public final boolean eagerFetching;
  public final UUID tenantId;
  public final Integer partitionCount;
  public final Integer partitionNumber;
  public final Set<String> types;

  private GetFeedRequest(Builder builder) {
    this.feedName = builder.feedName;
    this.limit = builder.limit;
    this.waitTime = builder.waitTime;
    this.eagerFetching = builder.eagerFetching;
    this.tenantId = builder.tenantId;
    this.partitionCount = builder.partitionCount;
    this.partitionNumber = builder.partitionNumber;
    this.types = Collections.unmodifiableSet(builder.types);
  }

  public boolean hasTenantId() {
    return tenantId != null;
  }

  public static class Builder {

    private static final ValueRange WAIT_TIME_VALUE_RANGE = ValueRange.of(0, 60);

    private final Set<String> types = new LinkedHashSet<>();
    private Integer limit;
    private String feedName = "_all";
    private Duration waitTime = Duration.ofSeconds(0);
    private boolean eagerFetching = true;
    private UUID tenantId;
    private Integer partitionCount;
    private Integer partitionNumber;

    /**
     * @param types Aggregate types to filter (include) when requesting the _all feed.
     */
    public Builder withTypes(String... types) {
      this.types.addAll(Arrays.asList(types));
      return this;
    }

    /**
     * @param feedName Name of feed to request
     */
    public Builder withFeed(String feedName) {
      this.feedName = feedName;
      return this;
    }

    public Builder withTenantId(UUID tenantId) {
      this.tenantId = tenantId;
      return this;
    }

    /**
     * @param limit Maximum number of returned feed entries per server response.
     */
    public Builder withLimit(int limit) {
      this.limit = limit;
      return this;
    }

    /**
     * @param eagerFetching True if the client should continue to fetch event within the same poll as long as there
     *                      are more available. Default is true.
     */
    public Builder withEagerFetching(boolean eagerFetching) {
      this.eagerFetching = eagerFetching;
      return this;
    }

    /**
     * @param waitTime If set to greater than 0, long polling will be enabled and each poll request will
     *                 hang either until a new entry was added to the feed or until the request times out.
     *                 Value must be between 0s and 60s, default is 0s.
     */
    public Builder withWaitTime(Duration waitTime) {
      if (WAIT_TIME_VALUE_RANGE.isValidValue(waitTime.get(ChronoUnit.SECONDS))) {
        this.waitTime = waitTime;
        return this;
      } else {
        throw new IllegalArgumentException(format("waitTime must be within %d and %d seconds",
            WAIT_TIME_VALUE_RANGE.getMinimum(), WAIT_TIME_VALUE_RANGE.getMaximum()));
      }
    }

    /**
     * Partitioned feeding enables parallel processing of events.
     * The partitioning is internally based on the hashCode of the aggregateId.
     *
     * @param partitionCount  The expected total number of partitions, i.e. the total number of consumers feeding in parallel.
     * @param partitionNumber The number of the partition to request.
     *                        Eg. if the {@link #partitionCount} is set to '2' the partition '0' and '1' can be fetched by two feeding consumer respectively.
     */
    public Builder withPartitioning(int partitionCount, int partitionNumber) {
      Validate.isTrue(partitionCount > 1, "The total number of partitions must be greater than 1");
      Validate.isTrue(partitionNumber >= 0, "The partition number cannot be negative");
      Validate.isTrue(partitionNumber < partitionCount,
          "The partition number is expected to be between 0 and (partitionCount - 1), in this case: " + (partitionCount - 1));

      this.partitionCount = partitionCount;
      this.partitionNumber = partitionNumber;
      return this;
    }

    public GetFeedRequest build() {
      Validate.notNull(feedName, "'feedName' must be set");
      if (!feedName.equals("_all")) {
        Validate.isTrue(types.isEmpty(), "type filter is only applicable when requesting the _all feed");
      }
      return new GetFeedRequest(this);
    }
  }

}
