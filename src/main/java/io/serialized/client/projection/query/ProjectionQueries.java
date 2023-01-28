package io.serialized.client.projection.query;

public class ProjectionQueries {

  public static ListProjectionQuery.Builder list(String projectionName) {
    return new ListProjectionQuery.Builder(projectionName);
  }

  public static ListProjectionQuery.Builder search(String projectionName, SearchString searchString) {
    return new ListProjectionQuery.Builder(projectionName).withSearchString(searchString);
  }

  public static SingleProjectionQuery.Builder single(String projectionName) {
    return new SingleProjectionQuery.Builder(projectionName);
  }

  public static AggregatedProjectionQuery.Builder aggregated(String projectionName) {
    return new AggregatedProjectionQuery.Builder(projectionName);
  }

}
