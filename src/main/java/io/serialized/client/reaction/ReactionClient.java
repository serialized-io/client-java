package io.serialized.client.reaction;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.SerializedOkHttpClient;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.util.Optional;
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
   * List reactions.
   */
  public ListReactionsResponse listReactions(ListReactionsRequest request) {
    HttpUrl.Builder urlBuilder = pathForReactions();
    Optional.ofNullable(request.status).ifPresent(status -> urlBuilder.addQueryParameter("status", status));
    Optional.ofNullable(request.from).ifPresent(from -> urlBuilder.addQueryParameter("from", from));
    Optional.ofNullable(request.to).ifPresent(to -> urlBuilder.addQueryParameter("to", to));
    Optional.ofNullable(request.aggregateId).ifPresent(aggregateId -> urlBuilder.addQueryParameter("aggregateId", aggregateId.toString()));
    Optional.ofNullable(request.eventId).ifPresent(eventId -> urlBuilder.addQueryParameter("eventId", eventId.toString()));
    Optional.ofNullable(request.skip).ifPresent(skip -> urlBuilder.addQueryParameter("skip", String.valueOf(skip)));
    Optional.ofNullable(request.limit).ifPresent(limit -> urlBuilder.addQueryParameter("limit", String.valueOf(limit)));

    if (request.tenantId().isPresent()) {
      return client.get(urlBuilder.build(), ListReactionsResponse.class, request.tenantId);
    } else {
      return client.get(urlBuilder.build(), ListReactionsResponse.class);
    }
  }

  /**
   * Execute a reaction.
   */
  public void executeReaction(ExecuteReactionRequest request) {
    HttpUrl url = pathForReactionExecution(request.reactionId).build();
    if (request.tenantId().isPresent()) {
      client.post(url, "", request.tenantId);
    } else {
      client.post(url, "");
    }
  }

  /**
   * Delete a scheduled reaction.
   */
  public void deleteReaction(DeleteReactionRequest request) {
    HttpUrl url = pathForReaction(request.reactionId).build();
    if (request.tenantId().isPresent()) {
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

  private HttpUrl.Builder pathForReactions() {
    return apiRoot.newBuilder()
        .addPathSegment("reactions");
  }

  private HttpUrl.Builder pathForReaction(UUID reactionId) {
    return apiRoot.newBuilder()
        .addPathSegment("reactions")
        .addPathSegment(reactionId.toString());
  }

  private HttpUrl.Builder pathForReactionExecution(UUID reactionId) {
    return apiRoot.newBuilder()
        .addPathSegment("reactions")
        .addPathSegment(reactionId.toString())
        .addPathSegment("execute");
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
      this.httpClient = config.newHttpClient();
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
