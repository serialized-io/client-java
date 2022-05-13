package io.serialized.client.feed;

import java.util.List;

public interface FeedEntryBatchHandler {

  /**
   * Called for each received feed entry batch.
   * <p>
   * Add your processing logic by implementing this method.
   * <p>
   * NOTE: The implementation should be idempotent and adhere to 'at least once' processing.
   *
   * @param feedEntryBatch The entries to process
   */
  void handle(List<FeedEntry> feedEntryBatch);

}
