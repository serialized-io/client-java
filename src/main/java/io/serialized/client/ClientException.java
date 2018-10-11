package io.serialized.client;

public class ClientException extends RuntimeException {

  public ClientException(String message) {
    super(message);
  }

  public ClientException(String message, Throwable cause) {
    super(message, cause);
  }

}
