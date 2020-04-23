package io.serialized.client.feed;

public class FeedRequests {

  public static ListFeedsRequest.Builder listFeeds() {
    return new ListFeedsRequest.Builder();
  }

  public static GetSequenceNumberRequest.Builder getSequenceNumber() {
    return new GetSequenceNumberRequest.Builder();
  }

  public static GetFeedRequest.Builder getFromFeed(String feedName) {
    return new GetFeedRequest.Builder().withFeed(feedName);
  }

  public static GetFeedRequest.Builder getFromAll() {
    return new GetFeedRequest.Builder().withFeed("_all");
  }

}
