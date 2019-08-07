package io.serialized.client.aggregate;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Request {

  private final ObjectMapper objectMapper;

  public Request(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }


}
