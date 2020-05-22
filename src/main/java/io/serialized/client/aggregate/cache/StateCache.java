package io.serialized.client.aggregate.cache;

import java.util.Optional;
import java.util.UUID;

public interface StateCache<T> {

  void put(UUID aggregateId, VersionedState<T> versionedState);

  Optional<VersionedState<T>> get(UUID aggregateId);

  void invalidate(UUID aggregateId);

}
