package io.serialized.client.aggregates;

public interface EventHandler<T, E extends Event> {

  T handle(T state, Event<E> e);

}
