package io.serialized.client.reaction;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

public class ReactionsResponse {

  private List<Reaction> reactions;

  public List<Reaction> reactions() {
    return reactions == null ? emptyList() : unmodifiableList(reactions);
  }

}
