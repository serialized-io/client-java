package io.serialized.client.aggregate.cache;

public class VersionedState<T> {

  private final T state;
  private final long version;

  public VersionedState(T state, long version) {
    this.state = state;
    this.version = version;
  }

  public T state() {
    return state;
  }

  public long version() {
    return version;
  }

}
