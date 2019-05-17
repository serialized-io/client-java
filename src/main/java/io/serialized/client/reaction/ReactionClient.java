package io.serialized.client.reaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.SerializedOkHttpClient;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import java.io.IOException;

public class ReactionClient {

  private final SerializedOkHttpClient client;
  private final HttpUrl apiRoot;
  private final ObjectMapper objectMapper;

  private ReactionClient(ReactionClient.Builder builder) {
    this.client = new SerializedOkHttpClient(builder.httpClient, builder.objectMapper);
    this.apiRoot = builder.apiRoot;
    this.objectMapper = builder.objectMapper;
  }

  public static ReactionClient.Builder reactionClient(SerializedClientConfig config) {
    return new ReactionClient.Builder(config);
  }

  public void createDefinition(ReactionDefinition reactionDefinition) {
    HttpUrl url = pathForDefinition().build();
    client.post(url, reactionDefinition);
  }

  /**
   * Creates a Reaction definition from a JSON String value.
   *
   * @param jsonString a JSON String with a valid Reaction definition
   * @throws IOException if the given String is not a valid Reaction definition
   */
  public void createDefinition(String jsonString) throws IOException {
    ReactionDefinition reactionDefinition = objectMapper.readValue(jsonString, ReactionDefinition.class);
    createDefinition(reactionDefinition);
  }

  public void createOrUpdate(ReactionDefinition reactionDefinition) {
    String reactionName = reactionDefinition.getReactionName();
    HttpUrl url = pathForDefinition().addPathSegment(reactionName).build();
    client.put(url, reactionDefinition);
  }

  /**
   * Creates/updates a Reaction definition from a JSON String value.
   *
   * @param jsonString a JSON String with a valid Reaction definition
   * @throws IOException if the given String is not a valid Reaction definition
   */
  public void createOrUpdate(String jsonString) throws IOException {
    ReactionDefinition reactionDefinition = objectMapper.readValue(jsonString, ReactionDefinition.class);
    createOrUpdate(reactionDefinition);
  }

  public ReactionDefinition getDefinition(String reactionName) {
    HttpUrl url = pathForDefinition().addPathSegment(reactionName).build();
    return client.get(url, ReactionDefinition.class);
  }

  public ReactionDefinitions listDefinitions() {
    HttpUrl url = pathForDefinition().build();
    return client.get(url, ReactionDefinitions.class);
  }

  public void deleteDefinition(String reactionName) {
    HttpUrl url = pathForDefinition().addPathSegment(reactionName).build();
    client.delete(url);
  }

  private HttpUrl.Builder pathForDefinition() {
    return apiRoot.newBuilder()
        .addPathSegment("reactions")
        .addPathSegment("definitions");
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
