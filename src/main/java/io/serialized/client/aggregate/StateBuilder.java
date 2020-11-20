package io.serialized.client.aggregate;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class StateBuilder<T> {

  private final Class<T> stateClass;
  private final Map<String, EventHandler<T, ?>> handlers = new LinkedHashMap<>();
  private boolean failOnMissingHandler = true;
  private final Set<String> ignoredEventTypes = new LinkedHashSet<>();

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

  public <E> StateBuilder<T> withIgnoredEventTypes(Set<String> ignoredEventTypes) {
    this.ignoredEventTypes.addAll(ignoredEventTypes);
    return this;
  }

  public <E> StateBuilder<T> withFailOnMissingHandler(boolean failOnMissingHandler) {
    this.failOnMissingHandler = failOnMissingHandler;
    return this;
  }

  public void setFailOnMissingHandler(boolean failOnMissingHandler) {
    this.failOnMissingHandler = failOnMissingHandler;
  }

  public void setIgnoredEventTypes(Set<String> ignoredEventTypes) {
    this.ignoredEventTypes.addAll(ignoredEventTypes);
  }

  public T buildState(List<? extends Event> events) {
    try {
      return buildState(stateClass.newInstance(), events);
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException("Failed to build State", e);
    }
  }

  public T buildState(T currentState, List<? extends Event> events) {
    AtomicReference<T> data = new AtomicReference<>(currentState);
    events.forEach(e -> {
          EventHandler<T, ?> handler = handlers.get(e.eventType());
      if (handler == null) {
        if (failOnMissingHandler && !ignoredEventTypes.contains(e.eventType())) {
          throw new IllegalStateException("No matching handler for event type: " + e.eventType());
        }
      } else {
        T handle = (T) handler.handle(data.get(), e);
        data.set(handle);
      }
        }
    );
    return data.get();
  }
}
