package io.serialized.client.feed;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

public class FeedResponse {

  private List<FeedEntry> entries;
  private long currentSequenceNumber;
  private boolean hasMore;

  public FeedResponse() {
  }

  public FeedResponse(List<FeedEntry> entries) {
    this.entries = entries;
  }

  public FeedResponse(List<FeedEntry> entries, long currentSequenceNumber, boolean hasMore) {
    this.entries = entries;
    this.currentSequenceNumber = currentSequenceNumber;
    this.hasMore = hasMore;
  }

  /**
   * @return List of all events from all batches.
   */
  public List<Event> events() {
    return unmodifiableList(entries().stream().flatMap(e -> e.events().stream()).collect(toList()));
  }

  /**
   * @return The entries.
   */
  public List<FeedEntry> entries() {
    return entries == null ? emptyList() : unmodifiableList(entries);
  }

  /**
   * @return Current sequence number at feed head.
   */
  public long currentSequenceNumber() {
    return currentSequenceNumber;
  }

  /**
   * @return True if there are more events available in the feed.
   */
  public boolean hasMore() {
    return hasMore;
  }

}
