package io.serialized.client.aggregate.cache;

public class VersionedState<T> {

  private final T state;
  private final int version;

  public VersionedState(T state, int version) {
    this.state = state;
    this.version = version;
  }

  public T state() {
    return state;
  }

  public int version() {
    return version;
  }

}
