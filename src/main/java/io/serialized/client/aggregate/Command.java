package io.serialized.client.aggregate;

import java.util.List;

public interface Command<A> {

  List<Event> apply(A aggregate);

}
