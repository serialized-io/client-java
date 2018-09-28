package io.serialized.samples.client.projection;

public class ProjectionResponse<T> {

  private String projectionId;
  private long updatedAt;
  private T data;

  public String projectionId() {
    return projectionId;
  }

  public long updatedAt() {
    return updatedAt;
  }

  public T data() {
    return data;
  }
}
