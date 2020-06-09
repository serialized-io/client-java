package io.serialized.client.projection;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

public class ProjectionsResponse<T> {

  private List<ProjectionResponse<T>> projections;

  private boolean hasMore;

  public ProjectionsResponse() {
  }

  public ProjectionsResponse(List<ProjectionResponse<T>> projections) {
    this.projections = projections;
  }

  public ProjectionsResponse(List<ProjectionResponse<T>> projections, boolean hasMore) {
    this.projections = projections;
    this.hasMore = hasMore;
  }

  public List<ProjectionResponse<T>> projections() {
    return projections == null ? emptyList() : unmodifiableList(projections);
  }

  public boolean hasMore() {
    return hasMore;
  }

}
