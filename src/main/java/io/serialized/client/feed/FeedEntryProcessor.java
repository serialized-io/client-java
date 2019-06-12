package io.serialized.client.feed;

public interface FeedEntryProcessor {

  /**
   * Called for each received feed entry.
   * <p>
   * Add your processing logic by implementing this method.
   * <p>
   * NOTE: The implementation should be idempotent and adhere to 'at least once' processing.
   *
   * @param feedEntry The entry to process
   */
  void process(FeedEntry feedEntry);

  /**
   * Called after each successful processing of a feed entry.
   * <p>
   * Implement this method to keep track of which sequence number has been processed successfully.
   *
   * @param sequenceNumber Sequence number of last successfully processed entry.
   */
  void onSuccess(Long sequenceNumber);

}
