package io.serialized.samples.client;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.serialized.samples.client.aggregates.AggregatesApiClient;
import io.serialized.samples.client.aggregates.EventDeserializer;
import io.serialized.samples.client.aggregates.LoadAggregateDeserializer;
import io.serialized.samples.client.feed.FeedApiClient;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

public class SerializedApiClient {

  public static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

  private final FeedApiClient feedApiClient;
  private final AggregatesApiClient aggregatesApiClient;

  private SerializedApiClient(OkHttpClient httpClient, ObjectMapper objectMapper, HttpUrl apiRoot, Set<Class> eventTypes) {
    this.feedApiClient = new FeedApiClient(httpClient, objectMapper, apiRoot, eventTypes);
    this.aggregatesApiClient = new AggregatesApiClient(httpClient, objectMapper, apiRoot, eventTypes);
  }

  public static Builder builder() {
    return new Builder();
  }

  public FeedApiClient feedApi() {
    return feedApiClient;
  }

  public AggregatesApiClient aggregatesApi() {
    return aggregatesApiClient;
  }

  public static class Builder {

    private URI rootUrl = URI.create("https://api.serialized.io/)");
    private String accessKey;
    private String secretAccessKey;
    private ObjectMapper objectMapper = new ObjectMapper()
        .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        .setSerializationInclusion(NON_NULL);

    private Set<Class> eventTypes = new LinkedHashSet<>();

    public Builder rootApiUrl(String rootApiUrl) {
      rootUrl = URI.create(rootApiUrl);
      return this;
    }

    public Builder accessKey(String accessKey) {
      this.accessKey = accessKey;
      return this;
    }

    public Builder secretAccessKey(String secretAccessKey) {
      this.secretAccessKey = secretAccessKey;
      return this;
    }

    public Builder withObjectMapper(ObjectMapper objectMapper) {
      this.objectMapper = objectMapper;
      return this;
    }

    public Builder registerEventType(Class eventType) {
      this.eventTypes.add(eventType);
      return this;
    }

    public SerializedApiClient build() {

      objectMapper.registerModule(EventDeserializer.module(eventTypes));

      HttpUrl apiRoot = HttpUrl.get(rootUrl);
      OkHttpClient client = new OkHttpClient.Builder()
          .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
              .headers(new Headers.Builder()
                  .add("Serialized-Access-Key", accessKey)
                  .add("Serialized-Secret-Access-Key", secretAccessKey)
                  .build())
              .build()))
          .build();
      return new SerializedApiClient(client, objectMapper, apiRoot, eventTypes);
    }
  }

}
