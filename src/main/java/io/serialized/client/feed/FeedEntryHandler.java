package io.serialized.client.feed;

public interface FeedEntryHandler {

  /**
   * Called for each received feed entry.
   * <p>
   * Add your processing logic by implementing this method.
   * <p>
   * NOTE: The implementation should be idempotent and adhere to 'at least once' processing.
   *
   * @param feedEntry The entry to process
   */
  void handle(FeedEntry feedEntry);

}
