package io.serialized.client.aggregates;

public interface StateLoader<T> {

  void loadState(T event);

}
