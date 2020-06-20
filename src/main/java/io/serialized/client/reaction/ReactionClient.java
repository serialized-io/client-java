package io.serialized.client.reaction;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.SerializedOkHttpClient;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;

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
    HttpUrl url = pathForDefinitions().build();
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

  /**
   * Creates/updates a Reaction definition.
   * <p>
   * Note that this method is idempotent.
   */
  public void createOrUpdate(ReactionDefinition reactionDefinition) {
    String reactionName = reactionDefinition.reactionName();
    HttpUrl url = pathForDefinitions().addPathSegment(reactionName).build();
    client.put(url, reactionDefinition);
  }

  /**
   * Creates/updates a Reaction definition from a JSON String value.
   * <p>
   * Note that this method is idempotent.
   *
   * @param jsonString a JSON String with a valid Reaction definition
   * @throws IOException if the given String is not a valid Reaction definition
   */
  public void createOrUpdate(String jsonString) throws IOException {
    ReactionDefinition reactionDefinition = objectMapper.readValue(jsonString, ReactionDefinition.class);
    createOrUpdate(reactionDefinition);
  }

  /**
   * Get reaction definition.
   */
  public ReactionDefinition getDefinition(String reactionName) {
    HttpUrl url = pathForDefinitions().addPathSegment(reactionName).build();
    return client.get(url, ReactionDefinition.class);
  }

  /**
   * List all definitions.
   */
  public ReactionDefinitions listDefinitions() {
    HttpUrl url = pathForDefinitions().build();
    return client.get(url, ReactionDefinitions.class);
  }

  /**
   * Delete the definition and all related reactions.
   */
  public void deleteDefinition(String reactionName) {
    HttpUrl url = pathForDefinitions().addPathSegment(reactionName).build();
    client.delete(url);
  }

  /**
   * List triggered or scheduled reactions.
   */
  public ReactionsResponse listReactions(ReactionRequest request) {
    HttpUrl url = pathForReactions(request.type).build();
    if (request.hasTenantId()) {
      return client.get(url, ReactionsResponse.class, request.tenantId);
    } else {
      return client.get(url, ReactionsResponse.class);
    }
  }

  /**
   * Trigger a scheduled reaction or re-trigger an already triggered reaction.
   */
  public void triggerReaction(TriggerReactionRequest request) {
    HttpUrl url = pathForReaction(request.type, request.reactionId).build();
    if (request.hasTenantId()) {
      client.post(url, "", request.tenantId);
    } else {
      client.post(url, "");
    }
  }

  /**
   * Delete a scheduled reaction.
   */
  public void deleteReaction(DeleteReactionRequest request) {
    HttpUrl url = pathForReaction(request.type, request.reactionId).build();
    if (request.hasTenantId()) {
      client.delete(url, request.tenantId);
    } else {
      client.delete(url);
    }
  }

  private HttpUrl.Builder pathForDefinitions() {
    return apiRoot.newBuilder()
        .addPathSegment("reactions")
        .addPathSegment("definitions");
  }

  private HttpUrl.Builder pathForReactions(String type) {
    return apiRoot.newBuilder()
        .addPathSegment("reactions")
        .addPathSegment(type);
  }

  private HttpUrl.Builder pathForReaction(String type, UUID reactionId) {
    return apiRoot.newBuilder()
        .addPathSegment("reactions")
        .addPathSegment(type)
        .addPathSegment(reactionId.toString());
  }

  public static class Builder {

    private final ObjectMapper objectMapper = new ObjectMapper()
        .disable(FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(FAIL_ON_EMPTY_BEANS)
        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        .setSerializationInclusion(NON_NULL);

    private final OkHttpClient httpClient;
    private final HttpUrl apiRoot;

    public Builder(SerializedClientConfig config) {
      this.httpClient = config.httpClient();
      this.apiRoot = config.apiRoot();
    }

    /**
     * Allows object mapper customization.
     */
    public Builder configureObjectMapper(Consumer<ObjectMapper> consumer) {
      consumer.accept(objectMapper);
      return this;
    }

    public ReactionClient build() {
      return new ReactionClient(this);
    }

  }

}
