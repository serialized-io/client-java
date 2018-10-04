package io.serialized.client.aggregates;

import io.serialized.client.SerializedClientConfig;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class StateLoader<T extends State> {

  private final Class<T> stateClass;
  private final Map<String, EventHandler> handlers;
  private final String aggregateType;
  private final AggregatesApiClient client;

  private StateLoader(Builder<T> builder, AggregatesApiClient client) {
    this.aggregateType = builder.aggregateType;
    this.stateClass = builder.stateClass;
    this.handlers = builder.handlers;
    this.client = client;
  }

  public T buildState(List<? extends Event> events) {
    try {
      AtomicReference<T> currentState = new AtomicReference<>(stateClass.newInstance());
      events.forEach(e -> {
            String simpleName = e.data().getClass().getSimpleName();
            EventHandler handler = handlers.get(simpleName);
            T handle = (T) handler.handle(currentState.get(), e);
            currentState.set(handle);
          }
      );
      return currentState.get();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException("Failed to build State");
    }
  }

  public static <T extends State> Builder<T> stateLoader(Class<T> stateClass, SerializedClientConfig serializedClientConfig) {
    return new Builder<>(stateClass, serializedClientConfig);
  }

  public T loadState(String aggregateId) {
    LoadAggregateResponse loadAggregateResponse = client.loadEvents(aggregateType, aggregateId);
    return buildState(loadAggregateResponse.events());
  }

  public static class Builder<T extends State> {

    private final Class<T> stateClass;
    private final AggregatesApiClient.Builder clientBuilder;
    public String aggregateType;
    private Map<String, EventHandler> handlers = new HashMap<>();
    private Set<Class> eventTypes = new HashSet<>();

    public Builder(Class<T> stateClass, SerializedClientConfig serializedClientConfig) {
      this.clientBuilder = AggregatesApiClient.aggregatesClient(serializedClientConfig);
      this.stateClass = stateClass;
    }

    public Builder<T> forAggregateType(String aggregateType) {
      this.aggregateType = aggregateType;
      return this;
    }

    public <E extends Event> Builder<T> registerHandler(Class<E> eventType, EventHandler<T, E> handler) {
      this.clientBuilder.registerEventType(eventType);
      this.handlers.put(eventType.getSimpleName(), handler);
      return this;
    }

    public StateLoader<T> build() {
      return new StateLoader<>(this, clientBuilder.build());
    }
  }

}