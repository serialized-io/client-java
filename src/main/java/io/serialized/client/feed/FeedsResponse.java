package io.serialized.client.feed;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

public class FeedsResponse {

  private List<Feed> feeds;

  public FeedsResponse() {
  }

  public FeedsResponse(List<Feed> feeds) {
    this.feeds = feeds;
  }

  public List<Feed> feeds() {
    return feeds == null ? emptyList() : unmodifiableList(feeds);
  }

}
