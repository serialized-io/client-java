package io.serialized.client.aggregate;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public interface UpdateStrategy {

  /**
   * The default strategy will cause update to fail if no matching handler is found for the loaded event type.
   */
  UpdateStrategy DEFAULT = new UpdateStrategy.Builder().withFailOnMissingHandler(true).build();

  boolean failOnMissingHandler();

  Set<String> ignoredEventTypes();

  class Builder {

    private boolean failOnMissingHandler = true;
    private final Set<String> ignoredEventTypes = new LinkedHashSet<>();

    public Builder() {
    }

    /**
     * Decides if the update should fail if no matching handler is found for the loaded event type.
     */
    public Builder withFailOnMissingHandler(boolean failOnMissingHandler) {
      this.failOnMissingHandler = failOnMissingHandler;
      return this;
    }

    /**
     * @param eventTypes List of event types to ignore during update.
     */
    public Builder withIgnoredEventTypes(String... eventTypes) {
      this.ignoredEventTypes.addAll(Arrays.asList(eventTypes));
      return this;
    }

    public UpdateStrategy build() {
      return new UpdateStrategy() {
        @Override
        public boolean failOnMissingHandler() {
          return failOnMissingHandler;
        }

        @Override
        public Set<String> ignoredEventTypes() {
          return ignoredEventTypes;
        }
      };
    }
  }

}

