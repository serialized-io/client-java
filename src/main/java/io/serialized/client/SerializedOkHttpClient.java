package io.serialized.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.function.Function;

import static io.serialized.client.SerializedClientConfig.JSON_MEDIA_TYPE;
import static okhttp3.RequestBody.create;

public class SerializedOkHttpClient {

  private final OkHttpClient httpClient;
  private final ObjectMapper objectMapper;

  public SerializedOkHttpClient(OkHttpClient httpClient, ObjectMapper objectMapper) {
    this.httpClient = httpClient;
    this.objectMapper = objectMapper;
  }

  public void put(HttpUrl url, Object payload) {
    execute(new Request.Builder().url(url).put(create(JSON_MEDIA_TYPE, toJson(payload))).build(), res -> null);
  }

  public void post(HttpUrl url, Object payload) {
    execute(new Request.Builder().url(url).post(create(JSON_MEDIA_TYPE, toJson(payload))).build(), res -> null);
  }

  public void delete(HttpUrl url) {
    execute(new Request.Builder().url(url).delete().build(), res -> null);
  }

  public <T> T head(HttpUrl url, Function<Response, T> handler) {
    return execute(new Request.Builder().url(url).head().build(), handler);
  }

  public <T> T get(HttpUrl url, Class<T> responseClass) {
    return get(url, contents -> parseJsonAs(contents, responseClass));
  }

  public <T> T get(HttpUrl url, JavaType type) {
    return get(url, contents -> parseJsonAs(contents, type));
  }

  private <T> T get(HttpUrl url, Function<String, T> contentParser) {
    return execute(new Request.Builder().url(url).get().build(), response -> {
      try {
        String responseContents = response.body().string();
        return contentParser.apply(responseContents);
      } catch (IOException e) {
        throw new ClientException(e);
      }
    });
  }

  private <T> T execute(Request request, Function<Response, T> handler) {
    try (Response res = httpClient.newCall(request).execute()) {
      if (!res.isSuccessful()) {
        throw new ApiException(res.code(), nullSafeBody(res));
      }
      return handler.apply(res);
    } catch (IOException e) {
      throw new ClientException(e);
    }
  }

  private String nullSafeBody(Response res) throws IOException {
    return res.body() != null ? res.body().string() : res.message();
  }

  private <T> T parseJsonAs(String contents, Class<T> responseClass) {
    try {
      return objectMapper.readValue(contents, responseClass);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private <T> T parseJsonAs(String contents, JavaType type) {
    try {
      return objectMapper.readValue(contents, type);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String toJson(Object payload) {
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException e) {
      throw new ClientException(e);
    }
  }

}
