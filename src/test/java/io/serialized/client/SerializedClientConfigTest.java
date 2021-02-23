package io.serialized.client;

import org.junit.jupiter.api.Test;

import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;

public class SerializedClientConfigTest {

  @Test
  public void testDefaultHttpClientConfig() {
    SerializedClientConfig defaultConfig = SerializedClientConfig.serializedConfig()
        .accessKey("aaaaa")
        .secretAccessKey("bbbbb")
        .build();

    assertThat(defaultConfig.httpClient().readTimeoutMillis()).isEqualTo(60_000);
    assertThat(defaultConfig.httpClient().writeTimeoutMillis()).isEqualTo(10_000);
    assertThat(defaultConfig.httpClient().connectTimeoutMillis()).isEqualTo(10_000);
    assertThat(defaultConfig.httpClient().callTimeoutMillis()).isEqualTo(0);
  }

  @Test
  public void testCustomHttpClientConfig() {
    SerializedClientConfig defaultConfig = SerializedClientConfig.serializedConfig()
        .accessKey("aaaaa")
        .secretAccessKey("bbbbb")
        .configureHttpClient(builder -> {
          builder.readTimeout(ofSeconds(20));
          builder.writeTimeout(ofSeconds(20));
          builder.connectTimeout(ofSeconds(20));
          builder.callTimeout(ofSeconds(20));
        })
        .build();

    assertThat(defaultConfig.httpClient().readTimeoutMillis()).isEqualTo(20_000);
    assertThat(defaultConfig.httpClient().writeTimeoutMillis()).isEqualTo(20_000);
    assertThat(defaultConfig.httpClient().connectTimeoutMillis()).isEqualTo(20_000);
    assertThat(defaultConfig.httpClient().callTimeoutMillis()).isEqualTo(20_000);
  }

}
