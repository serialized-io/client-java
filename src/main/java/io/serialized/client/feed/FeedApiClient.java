package io.serialized.client.feed;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.serialized.client.SerializedClientConfig;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FeedApiClient {

  private final OkHttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final HttpUrl apiRoot;

  private FeedApiClient(Builder builder) {
    this.httpClient = builder.httpClient;
    this.objectMapper = builder.objectMapper;
    this.apiRoot = builder.apiRoot;
  }

  public List<Feed> listFeeds() throws IOException {
    Request request = new Request.Builder()
        .url(apiRoot.newBuilder().addPathSegment("feeds").build())
        .get()
        .build();

    FeedsResponse feedsResponse = readResponse(request, FeedsResponse.class);
    return feedsResponse.feeds();
  }

  public FeedResponse feed(String feedName) throws IOException {
    Request request = new Request.Builder()
        .url(apiRoot.newBuilder().addPathSegment("feeds").addPathSegment(feedName).build())
        .get()
        .build();

    return readResponse(request, FeedResponse.class);
  }

  private <T> T readResponse(Request request, Class<T> responseType) throws IOException {
    try (Response response = httpClient.newCall(request).execute()) {
      if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
      InputStream src = response.body().byteStream();
      return objectMapper.readValue(src, responseType);
    }
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
