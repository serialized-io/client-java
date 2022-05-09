package io.serialized.client.aggregate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

public class AggregateBulkRequest {

  public final List<AggregateRequest> batches;
  private final UUID tenantId;

  private AggregateBulkRequest(Builder builder) {
    this.batches = Collections.unmodifiableList(builder.batches);
    this.tenantId = builder.tenantId;
  }

  public Optional<UUID> tenantId() {
    return Optional.ofNullable(tenantId);
  }

  public BulkSaveEvents eventBatches() {
    return BulkSaveEvents.newBulkSaveEvents(batches.stream()
        .map(r -> r.eventBatch().withAggregateId(r.aggregateId))
        .collect(toList()));
  }

  public static Builder bulkRequest() {
    return new Builder();
  }

  public static class Builder {

    private final List<AggregateRequest> batches = new ArrayList<>();
    private UUID tenantId;

    public Builder withAggregateRequest(AggregateRequest request) {
      this.batches.add(request);
      return this;
    }

    public Builder withAggregateRequests(List<AggregateRequest> requests) {
      this.batches.addAll(requests);
      return this;
    }

    public Builder withTenantId(UUID tenantId) {
      this.tenantId = tenantId;
      return this;
    }

    public Builder withTenantId(String tenantId) {
      return this.withTenantId(UUID.fromString(tenantId));
    }

    public AggregateBulkRequest build() {

      if (batches.isEmpty()) {
        throw new IllegalStateException("batches is empty");
      }

      return new AggregateBulkRequest(this);
    }
  }

}
