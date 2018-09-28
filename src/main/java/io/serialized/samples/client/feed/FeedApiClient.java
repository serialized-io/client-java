package io.serialized.samples.client.feed;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class FeedApiClient {

  private final OkHttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final HttpUrl apiRoot;
  private final Set<Class> eventTypes;

  public FeedApiClient(OkHttpClient httpClient, ObjectMapper objectMapper, HttpUrl apiRoot, Set<Class> eventTypes) {
    this.httpClient = httpClient;
    this.objectMapper = objectMapper;
    this.apiRoot = apiRoot;
    this.eventTypes = eventTypes;
  }

  public FeedsResponse feeds() throws IOException {
    Request request = new Request.Builder()
        .url(apiRoot.newBuilder().addPathSegment("feeds").build())
        .get()
        .build();

    return readResponse(request, FeedsResponse.class);
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
}
