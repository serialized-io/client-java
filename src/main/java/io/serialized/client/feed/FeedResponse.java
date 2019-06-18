package io.serialized.client.feed;

import java.util.List;
import java.util.stream.Collectors;

public class FeedResponse {

  private List<FeedEntry> entries;
  private boolean hasMore;
  private long currentSequenceNumber;

  /**
   * @return List of all events from all batches.
   */
  public List<Event> events() {
    return entries.stream().flatMap(e -> e.events().stream()).collect(Collectors.toList());
  }

  /**
   * @return The entries.
   */
  public List<FeedEntry> entries() {
    return entries;
  }

  /**
   * @return True if there are more events available in the feed.
   */
  public boolean hasMore() {
    return hasMore;
  }

  /**
   * @return Current sequence number at feed head.
   */
  public long currentSequenceNumber() {
    return currentSequenceNumber;
  }

}
