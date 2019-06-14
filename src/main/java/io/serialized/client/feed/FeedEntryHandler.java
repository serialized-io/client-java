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
   * @throws RetryException to signal a retry as the current entry was not successfully processed.
   */
  void handle(FeedEntry feedEntry) throws RetryException;

}
