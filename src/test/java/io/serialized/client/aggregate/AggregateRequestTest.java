package io.serialized.client.aggregate;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.serialized.client.aggregate.Event.newEvent;
import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class AggregateRequestTest {

  @Test(expected = IllegalStateException.class)
  public void saveRequestMustHaveEvents() {
    AggregateRequest.saveRequest().withAggregateId(UUID.randomUUID()).build();
  }

  @Test(expected = IllegalStateException.class)
  public void saveRequestMustHaveAggregateId() {
    AggregateRequest.saveRequest().withEvent(newEvent("test-event").build()).build();
  }

  @Test
  public void createSaveRequestSuccessfully() {
    UUID aggregateId = UUID.randomUUID();
    AggregateRequest reqest = AggregateRequest.saveRequest().withAggregateId(aggregateId).withEvent(newEvent("test-event").build()).build();
    assertThat(reqest.aggregateId, is(aggregateId));
    assertThat(reqest.events.size(), is(1));
  }

  @Test
  public void createSaveRequestSuccessfullyWithMultipleEvents() {
    UUID aggregateId = UUID.randomUUID();
    List<Event> build = asList(newEvent("test-event").build(), newEvent("test-event").build());
    AggregateRequest request = AggregateRequest.saveRequest().withAggregateId(aggregateId).withEvents(build).build();
    assertThat(request.aggregateId, is(aggregateId));
    assertThat(request.events.size(), is(2));
  }

  @Test
  public void createSaveRequestSuccessfullyWithMultipleTypedEvents() {
    UUID aggregateId = UUID.randomUUID();
    Event<Map> event1 = newEvent(Map.class).data(new HashMap<>()).build();
    Event<Map> event2 = newEvent(Map.class).data(new HashMap<>()).build();
    List<Event<Map>> build = asList(event1, event2);
    AggregateRequest request = AggregateRequest.saveRequest().withAggregateId(aggregateId).withEvents(build).build();
    assertThat(request.aggregateId, is(aggregateId));
    assertThat(request.events.size(), is(2));
  }

  @Test
  public void createSaveRequestWithStringAsAggregateId() {
    String aggregateId = UUID.randomUUID().toString();
    AggregateRequest reqest = AggregateRequest.saveRequest().withAggregateId(aggregateId).withEvent(newEvent("test-event").build()).build();
    assertThat(reqest.aggregateId, is(UUID.fromString(aggregateId)));
    assertThat(reqest.events.size(), is(1));
  }

}
