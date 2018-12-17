package io.serialized.client.aggregate;

public interface EventHandler<T, E> {

  T handle(T state, Event<E> e);

}
