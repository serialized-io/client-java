package io.serialized.client.reaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.SerializedOkHttpClient;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

public class ReactionClient {

  private final SerializedOkHttpClient client;
  private final HttpUrl apiRoot;
  private final ObjectMapper objectMapper;

  private ReactionClient(ReactionClient.Builder builder) {
    this.client = new SerializedOkHttpClient(builder.httpClient, builder.objectMapper);
    this.apiRoot = builder.apiRoot;
    this.objectMapper = builder.objectMapper;
  }

  public void createOrUpdate(ReactionDefinition reactionDefinition) {
    HttpUrl url = apiRoot.newBuilder()
        .addPathSegment("reactions")
        .addPathSegment("definitions")
        .addPathSegment(reactionDefinition.reactionName()).build();

    client.put(url, reactionDefinition);
  }


  public static ReactionClient.Builder reactionClient(SerializedClientConfig config) {
    return new ReactionClient.Builder(config);
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

    public ReactionClient build() {
      return new ReactionClient(this);
    }
  }


}
