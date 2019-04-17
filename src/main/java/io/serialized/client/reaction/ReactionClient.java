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
    HttpUrl url = pathForDefinition(reactionDefinition.getReactionName());
    client.put(url, reactionDefinition);
  }

  public ReactionDefinition getDefinition(String reactionName) {
    HttpUrl url = pathForDefinition(reactionName);
    return client.get(url, ReactionDefinition.class);
  }

  public void deleteDefinition(String reactionName) {
    HttpUrl url = pathForDefinition(reactionName);
    client.delete(url);
  }

  private HttpUrl pathForDefinition(String reactionName) {
    return apiRoot.newBuilder()
        .addPathSegment("reactions")
        .addPathSegment("definitions")
        .addPathSegment(reactionName).build();
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
