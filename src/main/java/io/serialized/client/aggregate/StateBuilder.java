package io.serialized.client.aggregate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class StateBuilder<T> {

  private final Class<T> stateClass;
  private final Map<String, EventHandler<T, ?>> handlers = new LinkedHashMap<>();

  private StateBuilder(Class<T> stateClass, Map<String, EventHandler<T, ?>> handlers) {
    this.stateClass = stateClass;
    this.handlers.putAll(handlers);
  }

  public static <T> StateBuilder<T> stateBuilder(Class<T> stateClass) {
    return new StateBuilder<>(stateClass, new LinkedHashMap<>());
  }

  public static <T> StateBuilder<T> stateBuilder(Class<T> stateClass, Map<String, EventHandler<T, ?>> handlers) {
    return new StateBuilder<>(stateClass, handlers);
  }

  public <E> StateBuilder<T> withHandler(Class<E> eventClass, EventHandler<T, E> handler) {
    this.handlers.put(eventClass.getSimpleName(), handler);
    return this;
  }

  public T buildState(List<? extends Event> events) {
    try {
      AtomicReference<T> data = new AtomicReference<>(stateClass.newInstance());
      events.forEach(e -> {
        String simpleName = e.data().getClass().getSimpleName();
        EventHandler<T, ?> handler = handlers.get(simpleName);

        if (handler == null) {
          throw new IllegalStateException("No matching handler for event type: " + simpleName);
        }

        T handle = (T) handler.handle(data.get(), e);
        data.set(handle);
          }
      );
      return data.get();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException("Failed to build State", e);
    }
  }

}
