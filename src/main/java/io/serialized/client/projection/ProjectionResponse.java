package io.serialized.client.projection;

public class ProjectionResponse<T> {

  public String projectionId;
  public long updatedAt;
  public T data;

}
