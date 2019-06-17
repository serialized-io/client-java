package io.serialized.client;

public class ConcurrencyException extends ApiException {

  public ConcurrencyException(int statusCode, String message) {
    super(statusCode, message);
  }

}
