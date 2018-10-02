package io.serialized.client.aggregates;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class StateBuilder<T extends State> {

  private final Map<String, EventHandler> handlers;
  private final Class<T> stateClass;

  private StateBuilder(Builder<T> builder) {
    handlers = builder.handlers;
    stateClass = builder.stateClass;
  }

  public static <T extends State> Builder<T> stateBuilder(Class<T> stateClass) {
    return new Builder<>(stateClass);
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

  public static class Builder<T extends State> {

    private final Class<T> stateClass;
    private Map<String, EventHandler> handlers = new HashMap<>();

    public Builder(Class<T> stateClass) {
      this.stateClass = stateClass;
    }

    <E extends Event> Builder<T> registerHandler(Class<E> eventType, EventHandler<T, E> handler) {
      return registerHandler(eventType.getSimpleName(), handler);
    }

    <E extends Event> Builder<T> registerHandler(String eventType, EventHandler<T, E> handler) {
      handlers.put(eventType, handler);
      return this;
    }

    public StateBuilder<T> build() {
      return new StateBuilder<>(this);
    }
  }

}
