package io.serialized.client.aggregate.cache;

import java.util.Optional;
import java.util.UUID;

public interface StateCache<T> {

  default void put(UUID aggregateId, VersionedState<T> versionedState) {
  }

  default void put(UUID aggregateId, UUID tenantId, VersionedState<T> versionedState) {
  }

  default Optional<VersionedState<T>> get(UUID aggregateId) {
    return Optional.empty();
  }

  default Optional<VersionedState<T>> get(UUID aggregateId, UUID tenantId) {
    return Optional.empty();
  }

}
