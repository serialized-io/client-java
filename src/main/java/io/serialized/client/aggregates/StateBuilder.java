package io.serialized.client.aggregates;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class StateBuilder<T extends State> {

  private Class<T> stateClass;
  private final Map<String, EventHandler> handlers;

  private StateBuilder(Class<T> stateClass, Map<String, EventHandler> handlers) {
    this.stateClass = stateClass;
    this.handlers = handlers;
  }

  public static <T extends State> StateBuilder<T> stateBuilder(Class<T> stateClass, Map<String, EventHandler> handlers) {
    return new StateBuilder<>(stateClass, handlers);
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
      throw new RuntimeException("Failed to build State", e);
    }
  }
}
