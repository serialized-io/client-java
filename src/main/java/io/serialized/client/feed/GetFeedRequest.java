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
  public final Duration pollDelay;
  public final boolean eagerFetching;
  public final UUID tenantId;
  public final Integer partitionCount;
  public final Integer partitionNumber;
  public final Set<String> types;

  private GetFeedRequest(Builder builder) {
    this.feedName = builder.feedName;
    this.limit = builder.limit;
    this.pollDelay = builder.pollDelay;
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

    private static final ValueRange SUBSCRIPTION_POLL_DELAY_VALUE_RANGE = ValueRange.of(1, 60);

    private final Set<String> types = new LinkedHashSet<>();
    private Integer limit;
    private String feedName = "_all";
    private Duration pollDelay = Duration.ofSeconds(1);
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
     * @param pollDelay Desired delay between feed polls. Must be between 1s and 60s. Default is 1s.
     */
    public Builder withSubscriptionPollDelay(Duration pollDelay) {
      if (SUBSCRIPTION_POLL_DELAY_VALUE_RANGE.isValidValue(pollDelay.get(ChronoUnit.SECONDS))) {
        this.pollDelay = pollDelay;
        return this;
      } else {
        throw new IllegalArgumentException(format("Poll delay must be within %d and %d seconds",
            SUBSCRIPTION_POLL_DELAY_VALUE_RANGE.getMinimum(), SUBSCRIPTION_POLL_DELAY_VALUE_RANGE.getMaximum()));
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
