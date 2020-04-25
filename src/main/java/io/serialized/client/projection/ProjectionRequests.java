package io.serialized.client.projection;

public class ProjectionRequests {

  public static ProjectionRequest.Builder single(String projectionName) {
    return new ProjectionRequest.Builder(ProjectionType.SINGLE).withProjectionName(projectionName);
  }

  public static ProjectionRequest.Builder aggregated(String projectionName) {
    return new ProjectionRequest.Builder(ProjectionType.AGGREGATED).withProjectionName(projectionName);
  }

}
