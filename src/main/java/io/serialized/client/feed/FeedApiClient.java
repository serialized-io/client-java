package io.serialized.client.feed;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.SerializedOkHttpClient;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import java.util.List;

public class FeedApiClient {

  private final HttpUrl apiRoot;
  private final SerializedOkHttpClient client;

  private FeedApiClient(Builder builder) {
    this.client = new SerializedOkHttpClient(builder.httpClient, builder.objectMapper);
    this.apiRoot = builder.apiRoot;
  }

  public List<Feed> listFeeds() {
    HttpUrl url = apiRoot.newBuilder().addPathSegment("feeds").build();
    return client.get(url, FeedsResponse.class).feeds();
  }

  public FeedResponse feed(String feedName) {
    HttpUrl url = apiRoot.newBuilder().addPathSegment("feeds").addPathSegment(feedName).build();

    return client.get(url, FeedResponse.class);

  }


  public static Builder feedClient(SerializedClientConfig config) {
    return new Builder(config);
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

    public FeedApiClient build() {
      return new FeedApiClient(this);
    }
  }

}
