package io.serialized.client.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.testing.junit5.DropwizardClientExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.serialized.client.ConcurrencyException;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.aggregate.AggregateApiStub;
import io.serialized.client.aggregate.AggregateClient;
import io.serialized.client.aggregate.AggregateExists;
import io.serialized.client.aggregate.AggregateRequest;
import io.serialized.client.aggregate.AggregateUpdate;
import io.serialized.client.aggregate.BulkSaveEvents;
import io.serialized.client.aggregate.Event;
import io.serialized.client.aggregate.EventBatch;
import io.serialized.client.aggregate.RetryStrategy;
import io.serialized.client.aggregate.UpdateStrategy;
import io.serialized.client.aggregate.cache.StateCache;
import io.serialized.client.aggregate.cache.VersionedState;
import io.serialized.client.aggregate.order.Order;
import io.serialized.client.aggregate.order.OrderCanceled;
import io.serialized.client.aggregate.order.OrderDeleted;
import io.serialized.client.aggregate.order.OrderPlaced;
import io.serialized.client.aggregate.order.OrderState;
import io.serialized.client.aggregate.order.OrderStatus;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static io.serialized.client.EventTypeMatcher.containsEventType;
import static io.serialized.client.aggregate.AggregateBulkRequest.bulkRequest;
import static io.serialized.client.aggregate.AggregateClient.aggregateClient;
import static io.serialized.client.aggregate.AggregateDelete.deleteRequest;
import static io.serialized.client.aggregate.AggregateRequest.saveRequest;
import static io.serialized.client.aggregate.Event.newEvent;
import static io.serialized.client.aggregate.order.OrderPlaced.orderPlaced;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
public class AggregateClientIT {

  private final AggregateApiStub.AggregateApiCallback apiCallback = mock(AggregateApiStub.AggregateApiCallback.class);

  public final DropwizardClientExtension dropwizard = new DropwizardClientExtension(new AggregateApiStub(apiCallback));

  @BeforeEach
  public void setUp() {
    dropwizard.getObjectMapper().setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
  }

  @Test
  public void testSave() {
    UUID orderId = UUID.fromString("723ecfce-14e9-4889-98d5-a3d0ad54912f");
    String aggregateType = "order";

    AggregateClient<OrderState> orderClient = aggregateClient(aggregateType, OrderState.class, getConfig())
        .registerHandler(OrderPlaced.class, OrderState::handleOrderPlaced)
        .build();

    when(apiCallback.eventsStored(eq(orderId), any(EventBatch.class))).thenReturn(OK);

    OrderState orderState = new OrderState();
    Order order = new Order(orderState);
    List<Event<?>> events = order.placeOrder(orderId, 123L);

    orderClient.save(saveRequest().withAggregateId(orderId).withEvents(events).build());

    verify(apiCallback, times(1)).eventsStored(eq(orderId), argThat(containsEventType("OrderPlaced")));
  }

  @Test
  public void testSaveBulk() {
    UUID orderId = UUID.fromString("723ecfce-14e9-4889-98d5-a3d0ad54912f");
    String aggregateType = "order";

    AggregateClient<OrderState> orderClient = aggregateClient(aggregateType, OrderState.class, getConfig())
        .registerHandler(OrderPlaced.class, OrderState::handleOrderPlaced)
        .build();

    when(apiCallback.eventBulkStored(any(BulkSaveEvents.class))).thenReturn(OK);

    OrderState orderState = new OrderState();
    Order order = new Order(orderState);
    List<Event<?>> events = order.placeOrder(orderId, 123L);

    orderClient.save(
        bulkRequest()
            .withAggregateRequest(saveRequest().withAggregateId(orderId).withEvents(events).build())
            .build()
    );

    verify(apiCallback, times(1)).eventBulkStored(any(BulkSaveEvents.class));
  }

  @Test
  public void testSaveRawEventType() {
    UUID orderId = UUID.fromString("723ecfce-14e9-4889-98d5-a3d0ad54912f");
    String aggregateType = "order";

    AggregateClient<Object> orderClient = aggregateClient(aggregateType, Object.class, getConfig()).build();

    when(apiCallback.eventsStored(eq(orderId), any(EventBatch.class))).thenReturn(OK);

    Event<?> event = newEvent("order-placed").data("orderId", orderId, "customerId", UUID.randomUUID()).build();

    orderClient.save(saveRequest().withAggregateId(orderId).withEvent(event).build());

    verify(apiCallback, times(1)).eventsStored(eq(orderId), argThat(containsEventType("order-placed")));
  }

  @Test
  public void testConcurrencyExceptionDuringSave() {
    UUID orderId = UUID.fromString("723ecfce-14e9-4889-98d5-a3d0ad54912f");
    String aggregateType = "order";

    AggregateClient<OrderState> orderClient = aggregateClient(aggregateType, OrderState.class, getConfig())
        .registerHandler(OrderPlaced.class, OrderState::handleOrderPlaced)
        .build();

    when(apiCallback.eventsStored(eq(orderId), any(EventBatch.class))).thenReturn(CONFLICT);

    OrderState orderState = new OrderState();
    Order order = new Order(orderState);
    List<Event<?>> events = order.placeOrder(orderId, 123L);

    assertThrows(ConcurrencyException.class, () ->
        orderClient.save(saveRequest().withAggregateId(orderId).withEvents(events).build())
    );
  }

  @Test
  public void testUpdate() throws IOException {
    UUID orderId = UUID.fromString("723ecfce-14e9-4889-98d5-a3d0ad54912f");
    String aggregateType = "order";

    AggregateClient<OrderState> orderClient = aggregateClient(aggregateType, OrderState.class, getConfig())
        .registerHandler(OrderPlaced.class, OrderState::handleOrderPlaced)
        .build();

    when(apiCallback.aggregateLoaded(aggregateType, orderId, 0, 1000)).thenReturn(getResource("/aggregate/placed_order1.json"));
    when(apiCallback.eventsStored(eq(orderId), any(EventBatch.class))).thenReturn(OK);

    assertThat(orderClient.update(orderId, orderState -> new Order(orderState).cancel())).isEqualTo(1);

    verify(apiCallback, times(1)).eventsStored(eq(orderId), argThat(containsEventType("OrderCanceled")));
  }

  @Test
  public void testBulkUpdate() throws IOException {
    UUID orderId1 = UUID.fromString("723ecfce-14e9-4889-98d5-a3d0ad54912f");
    UUID orderId2 = UUID.fromString("35b68baa-a891-4c44-af7c-f12743e777c3");
    Set<UUID> orderIds = new HashSet<>();
    orderIds.add(orderId1);
    orderIds.add(orderId2);

    String aggregateType = "order";

    AggregateClient<OrderState> orderClient = aggregateClient(aggregateType, OrderState.class, getConfig())
        .registerHandler(OrderPlaced.class, OrderState::handleOrderPlaced)
        .build();

    when(apiCallback.aggregateLoaded(aggregateType, orderId1, 0, 1000)).thenReturn(getResource("/aggregate/placed_order1.json"));
    when(apiCallback.aggregateLoaded(aggregateType, orderId2, 0, 1000)).thenReturn(getResource("/aggregate/placed_order2.json"));

    when(apiCallback.eventBulkStored(any(BulkSaveEvents.class))).thenReturn(OK);

    assertThat(orderClient.bulkUpdate(orderIds, orderState -> new Order(orderState).cancel())).isEqualTo(2);

    verify(apiCallback, times(1)).eventBulkStored(any(BulkSaveEvents.class));
  }

  @Test
  public void testBulkUpdateUsingStateCache() throws IOException {
    UUID orderId1 = UUID.fromString("723ecfce-14e9-4889-98d5-a3d0ad54912f");
    UUID orderId2 = UUID.fromString("35b68baa-a891-4c44-af7c-f12743e777c3");
    String aggregateType = "order";
    Set<UUID> orderIds = new HashSet<>();
    orderIds.add(orderId1);
    orderIds.add(orderId2);

    final Map<UUID, VersionedState<OrderState>> stateMap = new ConcurrentHashMap<>();
    StateCache<OrderState> stateCache = new StateCache<OrderState>() {

      @Override
      public void put(UUID aggregateId, VersionedState<OrderState> versionedState) {
        stateMap.put(aggregateId, versionedState);
      }

      @Override
      public Optional<VersionedState<OrderState>> get(UUID aggregateId) {
        return Optional.ofNullable(stateMap.get(aggregateId));
      }

      @Override
      public void invalidate(UUID aggregateId) {
        stateMap.remove(aggregateId);
      }

    };

    AggregateClient<OrderState> orderClient = aggregateClient(aggregateType, OrderState.class, getConfig())
        .registerHandler(OrderPlaced.class, OrderState::handleOrderPlaced)
        .registerHandler(OrderCanceled.class, OrderState::handleOrderCanceled)
        .registerHandler(OrderDeleted.class, OrderState::handleOrderDeleted)
        .build();

    when(apiCallback.aggregateLoaded(aggregateType, orderId1, 0, 1000)).thenReturn(getResource("/aggregate/placed_order1.json"));
    when(apiCallback.aggregateLoaded(aggregateType, orderId2, 0, 1000)).thenReturn(getResource("/aggregate/placed_order2.json"));
    when(apiCallback.eventBulkStored(any(BulkSaveEvents.class))).thenReturn(OK);

    int eventsStored = orderClient.bulkUpdate(orderIds, new AggregateUpdate<OrderState>() {

      @Override
      public Optional<StateCache<OrderState>> stateCache() {
        return Optional.of(stateCache);
      }

      @Override
      public List<Event<?>> apply(OrderState state) {
        return new Order(state).cancel();
      }
    });

    assertThat(eventsStored).isEqualTo(2);


    // Load from cache
    eventsStored = orderClient.bulkUpdate(orderIds, new AggregateUpdate<OrderState>() {
      @Override
      public Optional<StateCache<OrderState>> stateCache() {
        return Optional.of(stateCache);
      }

      @Override
      public List<Event<?>> apply(OrderState state) {
        Order order = new Order(state);
        return order.deleteOrder();
      }
    });

    assertThat(eventsStored).isEqualTo(2);

    verify(apiCallback, times(1)).aggregateLoaded("order", orderId1, 0, 1000);
    verify(apiCallback, times(1)).aggregateLoaded("order", orderId2, 0, 1000);

    ArgumentCaptor<EventBatch> secondCaptor = ArgumentCaptor.forClass(EventBatch.class);
    verify(apiCallback, times(2)).eventBulkStored(any(BulkSaveEvents.class));
  }

  @Test
  public void testUpdateUsingStateCache() throws IOException {
    UUID orderId = UUID.fromString("723ecfce-14e9-4889-98d5-a3d0ad54912f");
    String aggregateType = "order";

    final Map<UUID, VersionedState<OrderState>> stateMap = new ConcurrentHashMap<>();
    StateCache<OrderState> stateCache = new StateCache<OrderState>() {

      @Override
      public void put(UUID aggregateId, VersionedState<OrderState> versionedState) {
        stateMap.put(aggregateId, versionedState);
      }

      @Override
      public Optional<VersionedState<OrderState>> get(UUID aggregateId) {
        return Optional.ofNullable(stateMap.get(aggregateId));
      }

      @Override
      public void invalidate(UUID aggregateId) {
        stateMap.remove(aggregateId);
      }

    };

    AggregateClient<OrderState> orderClient = aggregateClient(aggregateType, OrderState.class, getConfig())
        .registerHandler(OrderPlaced.class, OrderState::handleOrderPlaced)
        .registerHandler(OrderCanceled.class, OrderState::handleOrderCanceled)
        .registerHandler(OrderDeleted.class, OrderState::handleOrderDeleted)
        .build();

    when(apiCallback.aggregateLoaded(aggregateType, orderId, 0, 1000)).thenReturn(getResource("/aggregate/placed_order1.json"));
    when(apiCallback.eventsStored(eq(orderId), any(EventBatch.class))).thenReturn(OK);

    int eventsStored = orderClient.update(orderId, new AggregateUpdate<OrderState>() {

      @Override
      public Optional<StateCache<OrderState>> stateCache() {
        return Optional.of(stateCache);
      }

      @Override
      public List<Event<?>> apply(OrderState state) {
        return new Order(state).cancel();
      }
    });

    assertThat(eventsStored).isEqualTo(1);

    ArgumentCaptor<EventBatch> firstCaptor = ArgumentCaptor.forClass(EventBatch.class);
    verify(apiCallback).eventsStored(eq(orderId), firstCaptor.capture());

    EventBatch eventsCaptured = firstCaptor.getValue();
    assertThat(eventsCaptured.events()).hasSize(1);
    Event<?> event = eventsCaptured.events().get(0);
    assertThat(event.eventType()).isEqualTo(OrderCanceled.class.getSimpleName());
    Map data = (Map) event.data();
    assertThat(data).containsKey("orderId");

    eventsStored = orderClient.update(orderId, new AggregateUpdate<OrderState>() {

      @Override
      public Optional<StateCache<OrderState>> stateCache() {
        return Optional.of(stateCache);
      }

      @Override
      public List<Event<?>> apply(OrderState state) {
        Order order = new Order(state);
        return order.deleteOrder();
      }
    });

    assertThat(eventsStored).isEqualTo(1);

    verify(apiCallback, times(1)).aggregateLoaded(anyString(), any(UUID.class), eq(0), eq(1000));

    ArgumentCaptor<EventBatch> secondCaptor = ArgumentCaptor.forClass(EventBatch.class);
    verify(apiCallback, times(2)).eventsStored(eq(orderId), secondCaptor.capture());

    eventsCaptured = secondCaptor.getValue();
    assertThat(eventsCaptured.events()).hasSize(1);
    event = eventsCaptured.events().get(0);
    assertThat(event.eventType()).isEqualTo(OrderDeleted.class.getSimpleName());
    data = (Map) event.data();
    assertThat(data).containsKey("orderId");
  }

  @Test
  public void testStateCacheIsInvalidatedOnConcurrencyException() throws IOException {
    UUID orderId = UUID.fromString("723ecfce-14e9-4889-98d5-a3d0ad54912f");
    String aggregateType = "order";

    final Map<UUID, VersionedState<OrderState>> stateMap = new ConcurrentHashMap<>();
    StateCache<OrderState> stateCache = new StateCache<OrderState>() {

      @Override
      public void put(UUID aggregateId, VersionedState<OrderState> versionedState) {
        stateMap.put(aggregateId, versionedState);
      }

      @Override
      public Optional<VersionedState<OrderState>> get(UUID aggregateId) {
        return Optional.ofNullable(stateMap.get(aggregateId));
      }

      @Override
      public void invalidate(UUID aggregateId) {
        stateMap.remove(aggregateId);
      }

    };

    AggregateClient<OrderState> orderClient = aggregateClient(aggregateType, OrderState.class, getConfig())
        .registerHandler(OrderPlaced.class, OrderState::handleOrderPlaced)
        .registerHandler(OrderCanceled.class, OrderState::handleOrderCanceled)
        .registerHandler(OrderDeleted.class, OrderState::handleOrderDeleted)
        .build();

    when(apiCallback.aggregateLoaded(aggregateType, orderId, 0, 1000)).thenReturn(getResource("/aggregate/placed_order1.json"));
    when(apiCallback.eventsStored(eq(orderId), any(EventBatch.class))).thenReturn(OK);

    assertThat(stateMap).doesNotContainKey(orderId);

    int eventsStored = orderClient.update(orderId, new AggregateUpdate<OrderState>() {

      @Override
      public Optional<StateCache<OrderState>> stateCache() {
        return Optional.of(stateCache);
      }

      @Override
      public List<Event<?>> apply(OrderState state) {
        return new Order(state).cancel();
      }
    });

    assertThat(eventsStored).isEqualTo(1);
    assertThat(stateMap).containsKey(orderId);

    ArgumentCaptor<EventBatch> firstCaptor = ArgumentCaptor.forClass(EventBatch.class);
    verify(apiCallback).eventsStored(eq(orderId), firstCaptor.capture());

    EventBatch eventsCaptured = firstCaptor.getValue();
    assertThat(eventsCaptured.events()).hasSize(1);
    Event<?> event = eventsCaptured.events().get(0);
    assertThat(event.eventType()).isEqualTo(OrderCanceled.class.getSimpleName());
    Map data = (Map) event.data();
    assertThat(data).containsKey("orderId");

    when(apiCallback.eventsStored(eq(orderId), any(EventBatch.class))).thenReturn(CONFLICT);

    assertThrows(ConcurrencyException.class, () ->

        orderClient.update(orderId, new AggregateUpdate<OrderState>() {

          @Override
          public Optional<StateCache<OrderState>> stateCache() {
            return Optional.of(stateCache);
          }

          @Override
          public List<Event<?>> apply(OrderState state) {
            Order order = new Order(state);
            return order.deleteOrder();
          }
        })
    );

    assertThat(stateMap).doesNotContainKey(orderId);
  }

  @Test
  public void testDeleteAggregateById() {
    UUID orderId = UUID.fromString("11111111-2222-3333-4444-555555555555");
    UUID deleteToken = UUID.fromString("99999999-9999-9999-9999-999999999999");
    String aggregateType = "order";

    AggregateClient<OrderState> orderClient = aggregateClient(aggregateType, OrderState.class, getConfig()).build();

    when(apiCallback.aggregateDeleteRequested(aggregateType, orderId)).thenReturn(ImmutableMap.of("deleteToken", deleteToken));

    orderClient.delete(deleteRequest().withAggregateId(orderId).build()).confirm();

    verify(apiCallback).aggregateDeletePerformed(aggregateType, orderId, deleteToken.toString());
  }

  @Test
  public void testDeleteAggregateByType() {
    UUID deleteToken = UUID.fromString("99999999-9999-9999-9999-999999999999");
    String aggregateType = "order";

    AggregateClient<OrderState> orderClient = aggregateClient(aggregateType, OrderState.class, getConfig()).build();

    when(apiCallback.aggregateTypeDeleteRequested(aggregateType)).thenReturn(ImmutableMap.of("deleteToken", deleteToken));

    orderClient.delete(deleteRequest().build()).confirm();

    verify(apiCallback).aggregateTypeDeletePerformed(aggregateType, deleteToken.toString());
  }

  @Test
  public void testConcurrencyExceptionDuringUpdate() throws IOException {
    UUID orderId = UUID.fromString("723ecfce-14e9-4889-98d5-a3d0ad54912f");
    String aggregateType = "order";

    AggregateClient<OrderState> orderClient = aggregateClient(aggregateType, OrderState.class, getConfig())
        .registerHandler(OrderPlaced.class, OrderState::handleOrderPlaced)
        .build();

    when(apiCallback.aggregateLoaded(aggregateType, orderId, 0, 1000)).thenReturn(getResource("/aggregate/placed_order1.json"));
    when(apiCallback.eventsStored(eq(orderId), any(EventBatch.class))).thenReturn(CONFLICT);

    assertThrows(ConcurrencyException.class, () ->
        orderClient.update(orderId, orderState -> new Order(orderState).cancel())
    );
  }

  @Test
  public void testRetryOnConcurrencyExceptionDuringUpdate() throws IOException {
    UUID orderId = UUID.fromString("723ecfce-14e9-4889-98d5-a3d0ad54912f");
    String aggregateType = "order";

    AggregateClient<OrderState> orderClient = aggregateClient(aggregateType, OrderState.class, getConfig())
        .registerHandler(OrderPlaced.class, OrderState::handleOrderPlaced)
        .registerHandler(OrderCanceled.class, OrderState::handleOrderCanceled)
        .withRetryStrategy(new RetryStrategy.Builder().withRetryCount(3).withSleepMs(10).build())
        .build();

    when(apiCallback.aggregateLoaded(aggregateType, orderId, 0, 1000)).thenReturn(getResource("/aggregate/placed_order1.json"));
    when(apiCallback.eventsStored(eq(orderId), any(EventBatch.class))).thenReturn(CONFLICT);

    assertThrows(ConcurrencyException.class, () ->
        orderClient.update(orderId, orderState -> new Order(orderState).cancel())
    );

    verify(apiCallback, times(4)).eventsStored(eq(orderId), any(EventBatch.class));
  }

  @Test
  public void testLoadAggregateState() throws IOException {
    UUID orderId = UUID.fromString("723ecfce-14e9-4889-98d5-a3d0ad54912f");
    String aggregateType = "order";

    AggregateClient<OrderState> orderClient = aggregateClient(aggregateType, OrderState.class, getConfig())
        .registerHandler(OrderPlaced.class, OrderState::handleOrderPlaced)
        .build();

    when(apiCallback.aggregateLoaded(aggregateType, orderId, 0, 1000)).thenReturn(getResource("/aggregate/placed_order1.json"));

    int eventsStored = orderClient.update(orderId, orderState -> {
      assertThat(orderState.status()).isEqualTo(OrderStatus.PLACED);
      return emptyList();
    });

    assertThat(eventsStored).isEqualTo(0);
  }

  @Test
  public void testLoadAggregateStateWithLimit() throws IOException {
    UUID orderId = UUID.fromString("723ecfce-14e9-4889-98d5-a3d0ad54912f");
    String aggregateType = "order";

    AggregateClient<OrderState> orderClient = aggregateClient(aggregateType, OrderState.class, getConfig())
        .withLimit(2)
        .registerHandler(OrderPlaced.class, OrderState::handleOrderPlaced)
        .registerHandler(OrderCanceled.class, OrderState::handleOrderCanceled)
        .registerHandler(OrderDeleted.class, OrderState::handleOrderDeleted)
        .build();

    when(apiCallback.aggregateLoaded(aggregateType, orderId, 0, 2)).thenReturn(getResource("/aggregate/shipped_order1.json"));
    when(apiCallback.aggregateLoaded(aggregateType, orderId, 2, 2)).thenReturn(getResource("/aggregate/shipped_order2.json"));

    int eventsStored = orderClient.update(orderId, orderState -> {
      assertThat(orderState.status()).isEqualTo(OrderStatus.DELETED);
      return emptyList();
    });

    assertThat(eventsStored).isEqualTo(0);
  }

  @Test
  public void testLoadAggregateStateMissingHandler() throws IOException {
    UUID orderId = UUID.fromString("723ecfce-14e9-4889-98d5-a3d0ad54912f");
    String aggregateType = "order";

    AggregateClient<OrderState> orderClient = aggregateClient(aggregateType, OrderState.class, getConfig())
        .build();

    when(apiCallback.aggregateLoaded(aggregateType, orderId, 0, 1000)).thenReturn(getResource("/aggregate/placed_order1.json"));

    Exception exception = assertThrows(IllegalStateException.class, () ->
        orderClient.update(orderId, orderState -> emptyList())
    );

    assertThat(exception.getMessage()).isEqualTo("No matching handler for event type: OrderPlaced");
  }

  @Test
  public void testLoadAggregateStateMissingHandlerIsIgnored() throws IOException {
    UUID orderId = UUID.fromString("723ecfce-14e9-4889-98d5-a3d0ad54912f");
    String aggregateType = "order";

    AggregateClient<OrderState> orderClient = aggregateClient(aggregateType, OrderState.class, getConfig())
        .withUpdateStrategy(new UpdateStrategy.Builder().withFailOnMissingHandler(false).build())
        .build();

    when(apiCallback.aggregateLoaded(aggregateType, orderId, 0, 1000)).thenReturn(getResource("/aggregate/placed_order1.json"));

    assertThat(orderClient.update(orderId, orderState -> emptyList())).isEqualTo(0);
  }

  @Test
  public void testLoadAggregateStateMissingHandlerIsIgnoredByType() throws IOException {
    UUID orderId = UUID.fromString("723ecfce-14e9-4889-98d5-a3d0ad54912f");
    String aggregateType = "order";

    AggregateClient<OrderState> orderClient = aggregateClient(aggregateType, OrderState.class, getConfig())
        .withUpdateStrategy(new UpdateStrategy.Builder()
            .withFailOnMissingHandler(true)
            .withIgnoredEventTypes("OrderPlaced")
            .build())
        .build();

    when(apiCallback.aggregateLoaded(aggregateType, orderId, 0, 1000)).thenReturn(getResource("/aggregate/placed_order1.json"));

    assertThat(orderClient.update(orderId, orderState -> emptyList())).isEqualTo(0);
  }

  @Test
  public void testAggregateExist() {
    UUID orderId = UUID.fromString("723ecfce-14e9-4889-98d5-a3d0ad54912f");
    String aggregateType = "order";

    AggregateClient<OrderState> orderClient = aggregateClient(aggregateType, OrderState.class, getConfig())
        .registerHandler(OrderPlaced.class, OrderState::handleOrderPlaced)
        .build();

    when(apiCallback.aggregateChecked(aggregateType, orderId)).thenReturn(true);

    assertTrue(orderClient.exists(AggregateExists.existsRequest().withAggregateId(orderId).build()));
    assertFalse(orderClient.exists(AggregateExists.existsRequest().withAggregateId(UUID.randomUUID()).build()));
  }

  @Test
  public void testStoreEvents() {

    AggregateClient<OrderState> orderClient = getOrderClient();

    UUID aggregateId = UUID.randomUUID();
    when(apiCallback.eventsStored(eq(aggregateId), any(EventBatch.class))).thenReturn(OK);

    orderClient.save(saveRequest().withAggregateId(aggregateId).withEvents(singletonList(orderPlaced("order-123", 1234L))).build());

    ArgumentCaptor<EventBatch> eventsStoredCaptor = ArgumentCaptor.forClass(EventBatch.class);
    verify(apiCallback).eventsStored(eq(aggregateId), eventsStoredCaptor.capture());

    EventBatch eventsStored = eventsStoredCaptor.getValue();
    List<Event<?>> events = eventsStored.events();
    assertThat(events).hasSize(1);
    Event<?> event = events.get(0);
    assertThat(event.eventType()).isEqualTo(OrderPlaced.class.getSimpleName());
    assertNotNull(event.data());
  }

  @Test
  public void testStoreEventsForTenant() {

    AggregateClient<OrderState> orderClient = getOrderClient();

    UUID aggregateId = UUID.randomUUID();
    UUID tenantId = UUID.randomUUID();
    List<Event<?>> events = singletonList(orderPlaced("order-123", 1234L));
    when(apiCallback.eventsStored(eq(aggregateId), any(EventBatch.class), any(UUID.class))).thenReturn(OK);

    AggregateRequest aggregateRequest = saveRequest().withTenantId(tenantId).withAggregateId(aggregateId).withEvents(events).build();
    orderClient.save(aggregateRequest);

    ArgumentCaptor<EventBatch> eventsStoredCaptor = ArgumentCaptor.forClass(EventBatch.class);
    verify(apiCallback).eventsStored(eq(aggregateId), eventsStoredCaptor.capture(), eq(tenantId));

    EventBatch eventsStored = eventsStoredCaptor.getValue();
    assertThat(eventsStored.events()).hasSize(1);
    Event<?> event = eventsStored.events().get(0);
    assertThat(event.eventType()).isEqualTo(OrderPlaced.class.getSimpleName());
    assertNotNull(event.data());
  }

  private AggregateClient<OrderState> getOrderClient() {
    return aggregateClient("order", OrderState.class, getConfig())
        .registerHandler("order-placed", OrderPlaced.class, OrderState::handleOrderPlaced)
        .build();
  }

  private SerializedClientConfig getConfig() {
    return SerializedClientConfig.serializedConfig()
        .rootApiUrl(dropwizard.baseUri() + "/api-stub/")
        .accessKey("aaaaa")
        .secretAccessKey("bbbbb").build();
  }

  private String getResource(String resource) throws IOException {
    return IOUtils.toString(getClass().getResourceAsStream(resource), UTF_8);
  }

}
