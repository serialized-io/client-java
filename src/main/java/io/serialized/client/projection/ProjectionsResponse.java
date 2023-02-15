package io.serialized.client.projection;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

public class ProjectionsResponse<T> implements Iterable<ProjectionResponse<T>> {

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

  @Override
  public Iterator<ProjectionResponse<T>> iterator() {
    return projections().iterator();
  }

  @Override
  public void forEach(Consumer<? super ProjectionResponse<T>> action) {
    projections().forEach(action);
  }

  @Override
  public Spliterator<ProjectionResponse<T>> spliterator() {
    return projections().spliterator();
  }

}
