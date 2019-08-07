package io.serialized.client.aggregate;

import org.junit.Test;

import java.util.UUID;

import static io.serialized.client.aggregate.AggregateRequest.appendRequest;
import static io.serialized.client.aggregate.Event.newEvent;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class AppendRequestTest {

  @Test
  public void createAppendRequest() {
    String aggregateId = UUID.randomUUID().toString();
    AggregateRequest request = appendRequest().withAggregateId(aggregateId).withEvent(newEvent("test-event").build()).build();
    assertThat(request.aggregateId, is(UUID.fromString(aggregateId)));
    assertThat(request.events.size(), is(1));
    assertThat(request.getEventBatch().getExpectedVersion(), nullValue());
  }

  @Test
  public void createAppendRequestForTenant() {
    UUID aggregateId = UUID.randomUUID();
    UUID tenantId = UUID.randomUUID();
    AggregateRequest request = appendRequest().withAggregateId(aggregateId).withEvent(newEvent("test-event").build()).withTenantId(tenantId).build();
    assertThat(request.aggregateId, is(aggregateId));
    assertThat(request.events.size(), is(1));
    assertThat(request.getTenantId().isPresent(), is(true));
    assertThat(request.getEventBatch().getExpectedVersion(), nullValue());
  }

  @Test
  public void createAppendRequestWithExpectedVersion() {
    UUID aggregateId = UUID.randomUUID();
    AggregateRequest request = appendRequest().withAggregateId(aggregateId).withEvent(newEvent("test-event").build()).withExpectedVersion(10).build();
    assertThat(request.aggregateId, is(aggregateId));
    assertThat(request.events.size(), is(1));
    assertThat(request.getEventBatch().getExpectedVersion(), is(10L));
  }

}
