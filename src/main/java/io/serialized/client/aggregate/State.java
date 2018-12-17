package io.serialized.client.aggregate;

public class State<D> {

  private long aggregateVersion;
  private D data;

  public State(long aggregateVersion, D data) {
    this.aggregateVersion = aggregateVersion;
    this.data = data;
  }

  public D data() {
    return data;
  }

  public long aggregateVersion() {
    return aggregateVersion;
  }

}
