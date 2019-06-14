package io.serialized.client.feed;

public class RetryException extends RuntimeException {

  public RetryException() {
  }

  public RetryException(String message) {
    super(message);
  }

  public RetryException(String message, Throwable cause) {
    super(message, cause);
  }
  
}
