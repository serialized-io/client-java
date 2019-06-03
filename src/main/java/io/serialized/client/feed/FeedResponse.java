package io.serialized.client.feed;

import java.util.List;
import java.util.stream.Collectors;

public class FeedResponse {

  private List<FeedEntry> entries;

  public List<Event> events() {
    return entries.stream().flatMap(e -> e.events().stream()).collect(Collectors.toList());
  }

  public List<FeedEntry> entries() {
    return entries;
  }

}
