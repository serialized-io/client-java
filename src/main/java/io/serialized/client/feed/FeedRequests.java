package io.serialized.client.feed;

public class FeedRequests {

  /**
   * @return A builder for constructing a request listing feeds.
   */
  public static ListFeedsRequest.Builder listFeeds() {
    return new ListFeedsRequest.Builder();
  }

  /**
   * @return A builder for constructing a request getting the current sequence number.
   */
  public static GetSequenceNumberRequest.Builder getSequenceNumber() {
    return new GetSequenceNumberRequest.Builder();
  }

  /**
   * @param feedName Name of feed, i.e aggregateType.
   * @return A builder for constructing a request getting events from a feed, for a given name/aggregateType.
   */
  public static GetFeedRequest.Builder getFromFeed(String feedName) {
    return new GetFeedRequest.Builder().withFeed(feedName);
  }

  /**
   * @return A builder for constructing a request getting events from the '_all' feed,
   * i.e all events, regardless of type, in guaranteed order.
   */
  public static GetFeedRequest.Builder getFromAll() {
    return new GetFeedRequest.Builder();
  }

  /**
   * @param types Aggregate types to filer (include)
   * @return A builder for constructing a request getting events from the '_all' feed,
   * i.e all events, filtered on given types, in guaranteed order.
   */
  public static GetFeedRequest.Builder getFromAll(String... types) {
    return new GetFeedRequest.Builder().withTypes(types);
  }

}
