package io.serialized.client.projection.query;

public class ProjectionQueries {

  public static ListProjectionQuery.Builder list(String projectionName) {
    return ListProjectionQuery.list(projectionName);
  }

  public static SingleProjectionQuery.Builder single(String projectionName) {
    return SingleProjectionQuery.singleProjection(projectionName);
  }

  public static AggregatedProjectionQuery.Builder aggregated(String projectionName) {
    return AggregatedProjectionQuery.aggregatedProjection(projectionName);
  }

}
