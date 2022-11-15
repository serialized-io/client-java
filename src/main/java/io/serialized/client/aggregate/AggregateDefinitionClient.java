package io.serialized.client.aggregate;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.serialized.client.SerializedClientConfig;
import io.serialized.client.SerializedOkHttpClient;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.util.function.Consumer;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;

public class AggregateDefinitionClient {

  private final SerializedOkHttpClient client;
  private final HttpUrl apiRoot;
  private final ObjectMapper objectMapper;

  private AggregateDefinitionClient(Builder builder) {
    this.client = new SerializedOkHttpClient(builder.httpClient, builder.objectMapper);
    this.apiRoot = builder.apiRoot;
    this.objectMapper = builder.objectMapper;
  }

  public static Builder aggregateClient(SerializedClientConfig config) {
    return new Builder(config);
  }

  /**
   * Creates an aggregate type definition from a JSON String value.
   *
   * @param jsonString a JSON String with a valid definition
   * @throws IOException if the given String is not a valid definition
   */
  public void createDefinition(String jsonString) throws IOException {
    AggregateTypeDefinition aggregateTypeDefinition = objectMapper.readValue(jsonString, AggregateTypeDefinition.class);
    createDefinition(aggregateTypeDefinition);
  }

  public void createDefinition(AggregateTypeDefinition aggregateTypeDefinition) {
    HttpUrl url = pathForDefinitions().build();
    client.post(url, aggregateTypeDefinition);
  }

  /**
   * Get definition.
   */
  public AggregateTypeDefinition getDefinition(String aggregateType) {
    HttpUrl url = pathForDefinitions().addPathSegment(aggregateType).build();
    return client.get(url, AggregateTypeDefinition.class);
  }

  /**
   * List all definitions.
   */
  public AggregateTypeDefinitions listDefinitions() {
    HttpUrl url = pathForDefinitions().build();
    return client.get(url, AggregateTypeDefinitions.class);
  }

  /**
   * Delete the definition.
   */
  public void deleteDefinition(String aggregateType) {
    HttpUrl url = pathForDefinitions().addPathSegment(aggregateType).build();
    client.delete(url);
  }

  private HttpUrl.Builder pathForDefinitions() {
    return apiRoot.newBuilder()
        .addPathSegment("aggregates")
        .addPathSegment("definitions");
  }

  public static class Builder {

    private final ObjectMapper objectMapper = new ObjectMapper()
        .disable(FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(FAIL_ON_EMPTY_BEANS)
        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        .setSerializationInclusion(NON_NULL);

    private final HttpUrl apiRoot;
    private final OkHttpClient httpClient;

    Builder(SerializedClientConfig config) {
      this.apiRoot = config.apiRoot();
      this.httpClient = config.newHttpClient();
    }

    /**
     * Allows object mapper customization.
     */
    public Builder configureObjectMapper(Consumer<ObjectMapper> consumer) {
      consumer.accept(objectMapper);
      return this;
    }

    public AggregateDefinitionClient build() {
      return new AggregateDefinitionClient(this);
    }
  }

}
