package io.serialized.client.feed;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.SerializedOkHttpClient;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import java.util.List;
import java.util.Optional;

public class FeedClient {

  private final SerializedOkHttpClient client;
  private final HttpUrl apiRoot;

  private FeedClient(Builder builder) {
    this.client = new SerializedOkHttpClient(builder.httpClient, builder.objectMapper);
    this.apiRoot = builder.apiRoot;
  }

  public List<Feed> listFeeds() {
    HttpUrl url = apiRoot.newBuilder().addPathSegment("feeds").build();
    return client.get(url, FeedsResponse.class).feeds();
  }

  public FeedRequest feed(String feedName) {
    return new FeedRequest(feedName);
  }

  public static Builder feedClient(SerializedClientConfig config) {
    return new Builder(config);
  }

  public class FeedRequest {

    private Integer limit;
    private Long since;
    private String feedName;

    private FeedRequest(String feedName) {
      this.feedName = feedName;
    }

    public FeedRequest limit(int limit) {
      this.limit = limit;
      return this;
    }

    public FeedRequest since(long since) {
      this.since = since;
      return this;
    }

    private HttpUrl.Builder url() {
      HttpUrl.Builder url = apiRoot.newBuilder().addPathSegment("feeds").addPathSegment(feedName);

      Optional.ofNullable(limit).ifPresent(l ->
          url.addQueryParameter("limit", String.valueOf(l))
      );

      Optional.ofNullable(since).ifPresent(s ->
          url.addQueryParameter("since", String.valueOf(s))
      );

      return url;
    }

    public FeedResponse execute() {
      return client.get(url().build(), FeedResponse.class);
    }

  }

  public static class Builder {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final HttpUrl apiRoot;

    Builder(SerializedClientConfig config) {
      this.httpClient = config.httpClient();
      this.objectMapper = config.objectMapper();
      this.apiRoot = config.apiRoot();
    }

    public FeedClient build() {
      return new FeedClient(this);
    }
  }

}
