package io.serialized.client;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SerializedClientConfigTest {

  @Test
  void addModuleToObjectMapper() {
    SerializedClientConfig config = SerializedClientConfig.serializedConfig()
        .accessKey(UUID.randomUUID().toString())
        .secretAccessKey(UUID.randomUUID().toString())
        .build();

    assertThat(config.objectMapper().getRegisteredModuleIds().size()).isEqualTo(0);
    Module module = dummyModule();
    config.objectMapper().registerModule(module);

    assertThat(config.objectMapper().getRegisteredModuleIds().size()).isEqualTo(1);
  }

  private Module dummyModule() {
    return new Module() {
        @Override
        public String getModuleName() {
          return "some module";
        }

        @Override
        public Version version() {
          return Version.unknownVersion();
        }

        @Override
        public void setupModule(SetupContext setupContext) {

        }
      };
  }
}
