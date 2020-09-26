package io.serialized.client.aggregate;

public interface RetryStrategy {

  RetryStrategy DEFAULT = new RetryStrategy.Builder().withRetryCount(0).withSleepMs(0).build();

  int getRetryCount();

  int getSleepMs();

  class Builder {

    private int retryCount;
    private int sleepMs;

    public Builder() {
    }

    public Builder withRetryCount(int retryCount) {
      this.retryCount = retryCount;
      return this;
    }

    public Builder withSleepMs(int sleepMs) {
      this.sleepMs = sleepMs;
      return this;
    }

    public RetryStrategy build() {
      return new RetryStrategy() {
        @Override
        public int getRetryCount() {
          return retryCount;
        }

        @Override
        public int getSleepMs() {
          return sleepMs;
        }
      };
    }
  }

}

