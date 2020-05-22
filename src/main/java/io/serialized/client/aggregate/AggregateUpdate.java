package io.serialized.client.aggregate;

import io.serialized.client.aggregate.cache.StateCache;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AggregateUpdate<T> {

  List<Event<?>> apply(T state);

  default Optional<StateCache<T>> stateCache() {
    return Optional.empty();
  }

  /**
   * Enable/disable optimistic concurrency control for aggregate updates.
   */
  default boolean useOptimisticConcurrencyOnUpdate() {
    return true;
  }

  default Optional<UUID> tenantId() {
    return Optional.empty();
  }

}
