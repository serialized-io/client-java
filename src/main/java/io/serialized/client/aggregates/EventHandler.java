package io.serialized.client.aggregates;

public interface EventHandler<T, E> {

  T handle(T state, Event<E> e);

}
