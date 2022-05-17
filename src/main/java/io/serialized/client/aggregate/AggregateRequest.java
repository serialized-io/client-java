package io.serialized.client.aggregate;

import io.serialized.client.InvalidRequestException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AggregateRequest {

  public static final int MAX_EVENTS_IN_BATCH = 64;

  public final UUID aggregateId;
  public final List<Event<?>> events;
  private final UUID tenantId;
  private final Integer expectedVersion;

  private AggregateRequest(Builder builder) {
    this.aggregateId = builder.aggregateId;
    this.events = Collections.unmodifiableList(builder.events);
    this.tenantId = builder.tenantId;
    this.expectedVersion = builder.expectedVersion;
  }

  public Optional<UUID> tenantId() {
    return Optional.ofNullable(tenantId);
  }

  public EventBatch eventBatch() {
    return new EventBatch(this.events, expectedVersion);
  }

  public static Builder saveRequest() {
    return new Builder(0);
  }

  public static Builder appendRequest(int expectedVersion) {
    return new Builder(expectedVersion);
  }

  public static Builder appendRequest() {
    return new Builder(null);
  }

  public static class Builder {

    private UUID aggregateId;
    private final List<Event<?>> events = new ArrayList<>();
    private UUID tenantId;
    private Integer expectedVersion;

    public Builder(Integer expectedVersion) {
      this.expectedVersion = expectedVersion;
    }

    public Builder withEvent(Event event) {
      this.events.add(event);
      return this;
    }

    public Builder withEvents(List<Event<?>> events) {
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

    public Builder withTenantId(String tenantId) {
      return this.withTenantId(UUID.fromString(tenantId));
    }

    public Builder withExpectedVersion(int expectedVersion) {
      this.expectedVersion = expectedVersion;
      return this;
    }

    public AggregateRequest build() {

      if (events.isEmpty()) {
        throw new IllegalStateException("events is empty");
      }

      if (aggregateId == null) {
        throw new IllegalStateException("aggregateId is null");
      }

      if (events.size() >= MAX_EVENTS_IN_BATCH) {
        throw new InvalidRequestException("Cannot store more than 64 events per batch");
      }

      return new AggregateRequest(this);
    }
  }

}
