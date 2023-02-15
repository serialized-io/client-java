package io.serialized.client.projection.query;

public class ProjectionQueries {

  public static ListProjectionsQuery.Builder list(String projectionName) {
    return new ListProjectionsQuery.Builder(projectionName);
  }

  public static ListProjectionsQuery.Builder listAll(String projectionName) {
    return new ListProjectionsQuery.Builder(projectionName).withAutoPagination(true).withLimit(1000);
  }

  public static ListProjectionsQuery.Builder search(String projectionName, SearchString searchString) {
    return new ListProjectionsQuery.Builder(projectionName).withSearchString(searchString);
  }

  public static SingleProjectionQuery.Builder single(String projectionName) {
    return new SingleProjectionQuery.Builder(projectionName);
  }

  public static AggregatedProjectionQuery.Builder aggregated(String projectionName) {
    return new AggregatedProjectionQuery.Builder(projectionName);
  }

}
