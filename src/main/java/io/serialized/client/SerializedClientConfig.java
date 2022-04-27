package io.serialized.client;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.Validate;

import java.net.URI;
import java.util.function.Consumer;

import static java.time.Duration.ofSeconds;

public class SerializedClientConfig {

  public static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
  public static final String HTTPS_API_SERIALIZED_IO = "https://api.serialized.io/";

  private final OkHttpClient.Builder httpClientBuilder;
  private final HttpUrl apiRoot;

  private SerializedClientConfig(OkHttpClient.Builder httpClientBuilder, HttpUrl apiRoot) {
    this.httpClientBuilder = httpClientBuilder;
    this.apiRoot = apiRoot;
  }

  public static Builder serializedConfig() {
    return new Builder();
  }

  public OkHttpClient newHttpClient() {
    return httpClientBuilder.build();
  }

  public HttpUrl apiRoot() {
    return apiRoot;
  }

  public static class Builder {

    private final OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
        .connectTimeout(ofSeconds(10))
        .readTimeout(ofSeconds(60));

    private URI rootApiUrl = URI.create(HTTPS_API_SERIALIZED_IO);
    private String accessKey;
    private String secretAccessKey;

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

    /**
     * Allows HTTP client customization.
     */
    public Builder configureHttpClient(Consumer<OkHttpClient.Builder> consumer) {
      consumer.accept(httpClientBuilder);
      return this;
    }

    public SerializedClientConfig build() {
      Validate.notNull(rootApiUrl, "'rootApiUrl' must be set");
      Validate.notEmpty(accessKey, "'accessKey' must be set");
      Validate.notEmpty(secretAccessKey, "'secretAccessKey' must be set");

      HttpUrl apiRoot = HttpUrl.get(rootApiUrl);
      httpClientBuilder.addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
          .addHeader("Serialized-Access-Key", accessKey)
          .addHeader("Serialized-Secret-Access-Key", secretAccessKey)
          .build()));

      return new SerializedClientConfig(httpClientBuilder, apiRoot);
    }
  }

}
