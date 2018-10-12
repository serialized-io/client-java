package io.serialized.client.aggregates;

import java.util.List;

public interface Command<A> {

  List<Event> apply(A aggregate);

}
