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
    try (Response res = httpClient.newCall(putRequest(url, payload)).execute()) {
      if (!res.isSuccessful()) {
        throw new ClientException("PUT failed: " + (res.body() != null ? res.body().string() : res.message()));
      }
    } catch (IOException e) {
      throw new ClientException("PUT failed", e);
    }
  }

  public void post(HttpUrl url, Object payload) {
    try (Response res = httpClient.newCall(postRequest(url, payload)).execute()) {
      if (!res.isSuccessful()) {
        throw new ClientException("POST failed: " + (res.body() != null ? res.body().string() : res.message()));
      }
    } catch (Exception e) {
      throw new ClientException("POST failed", e);
    }
  }

  public void delete(HttpUrl url) {
    try (Response res = httpClient.newCall(deleteRequest(url)).execute()) {
      if (!res.isSuccessful()) {
        throw new ClientException("DELETE failed: " + (res.body() != null ? res.body().string() : res.message()));
      }
    } catch (Exception e) {
      throw new ClientException("DELETE failed", e);
    }
  }

  public int head(HttpUrl url) {
    try (Response res = httpClient.newCall(headRequest(url)).execute()) {
      return res.code();
    } catch (Exception e) {
      throw new ClientException("HEAD failed", e);
    }
  }

  private <T> T get(HttpUrl url, Function<String, T> contentParser) {
    try (Response res = httpClient.newCall(getRequest(url)).execute()) {
      if (!res.isSuccessful()) {
        throw new ClientException("GET failed: " + (res.body() != null ? res.body().string() : res.message()));
      }
      String responseContents = res.body().string();
      return contentParser.apply(responseContents);
    } catch (Exception e) {
      throw new ClientException("GET failed", e);
    }
  }

  public <T> T get(HttpUrl url, Class<T> responseClass) {
    return get(url, contents -> parseJsonAs(contents, responseClass));
  }

  public <T> T get(HttpUrl url, JavaType type) {
    return get(url, contents -> parseJsonAs(contents, type));
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

  private Request postRequest(HttpUrl url, Object payload) throws JsonProcessingException {
    return new Request.Builder().url(url).post(create(JSON_MEDIA_TYPE, toJson(payload))).build();
  }

  private Request deleteRequest(HttpUrl url) {
    return new Request.Builder().url(url).delete().build();
  }

  private Request putRequest(HttpUrl url, Object payload) throws JsonProcessingException {
    return new Request.Builder().url(url).put(create(JSON_MEDIA_TYPE, toJson(payload))).build();
  }

  private Request getRequest(HttpUrl url) {
    return new Request.Builder().url(url).get().build();
  }

  private Request headRequest(HttpUrl url) {
    return new Request.Builder().url(url).head().build();
  }

  private String toJson(Object payload) throws JsonProcessingException {
    return objectMapper.writeValueAsString(payload);
  }

}
