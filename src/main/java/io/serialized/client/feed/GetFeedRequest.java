package io.serialized.client.feed;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.ValueRange;
import java.util.UUID;

import static java.lang.String.format;

public class GetFeedRequest {

  public final String feedName;
  public final Integer limit;
  public final Duration pollDelay;
  public final boolean eagerFetching;
  public final UUID tenantId;

  public GetFeedRequest(Builder builder) {
    this.feedName = builder.feedName;
    this.limit = builder.limit;
    this.pollDelay = builder.pollDelay;
    this.eagerFetching = builder.eagerFetching;
    this.tenantId = builder.tenantId;
  }

  public boolean hasTenantId() {
    return tenantId != null;
  }

  public static class Builder {

    private static final ValueRange SUBSCRIPTION_POLL_DELAY_VALUE_RANGE = ValueRange.of(1, 60);

    private Integer limit;
    private String feedName;
    private Duration pollDelay = Duration.ofSeconds(1);
    private boolean eagerFetching = true;
    private UUID tenantId;

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

    public GetFeedRequest build() {
      return new GetFeedRequest(this);
    }

  }

}
