package io.serialized.client.aggregate;

import java.util.List;

public interface AggregateUpdate<T> {

  List<Event<?>> apply(T state);

}
