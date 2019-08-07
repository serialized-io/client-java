package io.serialized.client.aggregate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AggregateRequest {

  public final UUID aggregateId;
  public final List<Event> events;
  private final UUID tenantId;
  private final Long expectedVersion;

  public AggregateRequest(Builder builder) {
    this.aggregateId = builder.aggregateId;
    this.events = builder.events;
    this.tenantId = builder.tenantId;
    this.expectedVersion = builder.expectedVersion;
  }

  public static Builder saveRequest() {
    return new Builder(0L);
  }

  public static Builder appendRequest(long expectedVersion) {
    return new Builder(expectedVersion);
  }

  public static Builder appendRequest() {
    return new Builder(null);
  }

  public Optional<UUID> getTenantId() {
    return Optional.ofNullable(tenantId);
  }

  public EventBatch getEventBatch() {
    return new EventBatch(this.events, expectedVersion);
  }

  public static class Builder {

    private UUID aggregateId;
    private List<Event> events = new ArrayList<>();
    private UUID tenantId;
    private Long expectedVersion;

    public Builder(Long expectedVersion) {
      this.expectedVersion = expectedVersion;
    }

    public Builder withEvent(Event event) {
      this.events.add(event);
      return this;
    }

    public Builder withEvents(List<? extends Event> events) {
      this.events.addAll(events);
      return this;
    }

    public Builder withAggregateId(UUID aggregateId) {
      this.aggregateId = aggregateId;
      return this;
    }

    public Builder withAggregateId(String aggregateId) {
      return this.withAggregateId(UUID.fromString(aggregateId));
    }

    public Builder withTenantId(UUID tenantId) {
      this.tenantId = tenantId;
      return this;
    }

    public Builder withExpectedVersion(long expectedVersion) {
      this.expectedVersion = expectedVersion;
      return this;
    }

    public Builder withTenantId(String tenantId) {
      return this.withTenantId(UUID.fromString(tenantId));
    }

    public AggregateRequest build() {

      if (events.isEmpty()) {
        throw new IllegalStateException("events is empty");
      }

      if (aggregateId == null) {
        throw new IllegalStateException("aggregateId is null");
      }

      return new AggregateRequest(this);
    }
  }

}
