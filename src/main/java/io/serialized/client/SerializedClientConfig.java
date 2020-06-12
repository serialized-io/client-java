package io.serialized.client;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.Validate;

import java.net.URI;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;

public class SerializedClientConfig {

  public static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
  public static final String HTTPS_API_SERIALIZED_IO = "https://api.serialized.io/";

  private final OkHttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final HttpUrl apiRoot;

  private SerializedClientConfig(OkHttpClient httpClient, ObjectMapper objectMapper, HttpUrl apiRoot) {
    this.httpClient = httpClient;
    this.objectMapper = objectMapper;
    this.apiRoot = apiRoot;
  }

  public static Builder serializedConfig() {
    return new Builder();
  }

  public OkHttpClient httpClient() {
    return httpClient;
  }

  public ObjectMapper objectMapper() {
    return objectMapper;
  }

  public HttpUrl apiRoot() {
    return apiRoot;
  }

  public static class Builder {

    private URI rootApiUrl = URI.create(HTTPS_API_SERIALIZED_IO);
    private String accessKey;
    private String secretAccessKey;

    private final ObjectMapper objectMapper = new ObjectMapper()
        .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(FAIL_ON_EMPTY_BEANS, false)
        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        .setSerializationInclusion(NON_NULL);

    public Builder rootApiUrl(String rootApiUrl) {
      this.rootApiUrl = URI.create(rootApiUrl);
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
      Validate.notNull(rootApiUrl, "'rootApiUrl' must be set");
      Validate.notEmpty(accessKey, "'accessKey' must be set");
      Validate.notEmpty(secretAccessKey, "'secretAccessKey' must be set");

      HttpUrl apiRoot = HttpUrl.get(rootApiUrl);
      OkHttpClient client = new OkHttpClient.Builder()
          .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
              .addHeader("Serialized-Access-Key", accessKey)
              .addHeader("Serialized-Secret-Access-Key", secretAccessKey)
              .build()))
          .build();
      return new SerializedClientConfig(client, objectMapper, apiRoot);
    }
  }

}
