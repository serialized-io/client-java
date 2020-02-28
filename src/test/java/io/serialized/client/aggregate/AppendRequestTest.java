package io.serialized.client.aggregate;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.serialized.client.aggregate.AggregateRequest.appendRequest;
import static io.serialized.client.aggregate.Event.newEvent;
import static org.assertj.core.api.Assertions.assertThat;

public class AppendRequestTest {

  @Test
  public void createAppendRequest() {
    String aggregateId = UUID.randomUUID().toString();
    AggregateRequest request = appendRequest().withAggregateId(aggregateId).withEvent(newEvent("test-event").build()).build();
    assertThat(request.aggregateId).isEqualTo(UUID.fromString(aggregateId));
    assertThat(request.events).hasSize(1);
    assertThat(request.getEventBatch().getExpectedVersion()).isNull();
  }

  @Test
  public void createAppendRequestForTenant() {
    UUID aggregateId = UUID.randomUUID();
    UUID tenantId = UUID.randomUUID();
    AggregateRequest request = appendRequest().withAggregateId(aggregateId).withEvent(newEvent("test-event").build()).withTenantId(tenantId).build();
    assertThat(request.aggregateId).isEqualTo(aggregateId);
    assertThat(request.events).hasSize(1);
    assertThat(request.getTenantId()).isPresent();
    assertThat(request.getEventBatch().getExpectedVersion()).isNull();
  }

  @Test
  public void createAppendRequestWithExpectedVersion() {
    UUID aggregateId = UUID.randomUUID();
    AggregateRequest request = appendRequest().withAggregateId(aggregateId).withEvent(newEvent("test-event").build()).withExpectedVersion(10).build();
    assertThat(request.aggregateId).isEqualTo(aggregateId);
    assertThat(request.events).hasSize(1);
    assertThat(request.getEventBatch().getExpectedVersion()).isEqualTo(10L);
  }

}
