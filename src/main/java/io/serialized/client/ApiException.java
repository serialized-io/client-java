package io.serialized.client;

public class ApiException extends RuntimeException {

  private final int statusCode;

  public ApiException(int statusCode, String message) {
    super(message);
    this.statusCode = statusCode;
  }

  public int statusCode() {
    return statusCode;
  }

}
