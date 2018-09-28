package io.serialized.client;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;

import java.net.URI;
import java.util.function.Supplier;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

public class SerializedClientConfig {

  public static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

  private final OkHttpClient httpClient;
  private final Supplier<ObjectMapper> objectMapper;
  private final HttpUrl apiRoot;

  private SerializedClientConfig(OkHttpClient httpClient, Supplier<ObjectMapper> objectMapper, HttpUrl apiRoot) {
    this.httpClient = httpClient;
    this.objectMapper = objectMapper;
    this.apiRoot = apiRoot;
  }

  public static Builder builder() {
    return new Builder();
  }

  public OkHttpClient httpClient() {
    return httpClient;
  }

  public ObjectMapper objectMapper() {
    return objectMapper.get();
  }

  public HttpUrl apiRoot() {
    return apiRoot;
  }

  public static class Builder {

    private URI rootUrl = URI.create("https://api.serialized.io/)");
    private String accessKey;
    private String secretAccessKey;

    private Supplier<ObjectMapper> objectMapper = () -> new ObjectMapper()
        .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        .setSerializationInclusion(NON_NULL);

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

    public SerializedClientConfig build() {
      HttpUrl apiRoot = HttpUrl.get(rootUrl);
      OkHttpClient client = new OkHttpClient.Builder()
          .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
              .headers(new Headers.Builder()
                  .add("Serialized-Access-Key", accessKey)
                  .add("Serialized-Secret-Access-Key", secretAccessKey)
                  .build())
              .build()))
          .build();
      return new SerializedClientConfig(client, objectMapper, apiRoot);
    }
  }

}
