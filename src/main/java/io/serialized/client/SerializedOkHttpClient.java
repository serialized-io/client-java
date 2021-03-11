package io.serialized.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Function;

import static io.serialized.client.SerializedClientConfig.JSON_MEDIA_TYPE;
import static okhttp3.RequestBody.create;

public class SerializedOkHttpClient {

  public static final String SERIALIZED_TENANT_ID = "Serialized-Tenant-Id";

  private final OkHttpClient httpClient;
  private final ObjectMapper objectMapper;

  public SerializedOkHttpClient(OkHttpClient httpClient, ObjectMapper objectMapper) {
    this.httpClient = httpClient;
    this.objectMapper = objectMapper;
  }

  public void put(HttpUrl url, Object payload) {
    execute(putRequest(url, payload).build(), res -> null);
  }

  public void put(HttpUrl url, Object payload, UUID tenantId) {
    execute(putRequest(url, payload).header(SERIALIZED_TENANT_ID, tenantId.toString()).build(), res -> null);
  }

  public void post(HttpUrl url, Object payload) {
    execute(postRequest(url, payload).build(), res -> null);
  }

  public void post(HttpUrl url, Object payload, UUID tenantId) {
    execute(postRequest(url, payload).header(SERIALIZED_TENANT_ID, tenantId.toString()).build(), res -> null);
  }

  public void delete(HttpUrl url) {
    execute(deleteRequest(url).build(), res -> null);
  }

  public <T> T delete(HttpUrl url, Class<T> responseClass) {
    return executeAndGet(deleteRequest(url), contents -> parseJsonAs(contents, responseClass));
  }

  public <T> T delete(HttpUrl url, UUID tenantId) {
    return executeAndGet(deleteRequest(url).header(SERIALIZED_TENANT_ID, tenantId.toString()), res -> null);
  }

  public <T> T delete(HttpUrl url, Class<T> responseClass, UUID tenantId) {
    return executeAndGet(deleteRequest(url).header(SERIALIZED_TENANT_ID, tenantId.toString()), contents -> parseJsonAs(contents, responseClass));
  }

  public <T> T head(HttpUrl url, Function<Response, T> handler) {
    return execute(headRequest(url).build(), handler);
  }

  public <T> T head(HttpUrl url, Function<Response, T> handler, UUID tenantId) {
    return execute(headRequest(url).header(SERIALIZED_TENANT_ID, tenantId.toString()).build(), handler);
  }

  public <T> T get(HttpUrl url, Class<T> responseClass) {
    return executeAndGet(getRequest(url), contents -> parseJsonAs(contents, responseClass));
  }

  public <T> T get(HttpUrl url, Class<T> responseClass, UUID tenantId) {
    return executeAndGet(getRequest(url).header(SERIALIZED_TENANT_ID, tenantId.toString()), contents -> parseJsonAs(contents, responseClass));
  }

  public <T> T get(HttpUrl url, JavaType type) {
    return executeAndGet(getRequest(url), contents -> parseJsonAs(contents, type));
  }

  public <T> T get(HttpUrl url, JavaType type, UUID tenantId) {
    return executeAndGet(getRequest(url).header(SERIALIZED_TENANT_ID, tenantId.toString()), contents -> parseJsonAs(contents, type));
  }

  private Request.Builder putRequest(HttpUrl url, Object payload) {
    return new Request.Builder().url(url).put(create(JSON_MEDIA_TYPE, toJson(payload)));
  }

  private Request.Builder postRequest(HttpUrl url, Object payload) {
    return new Request.Builder().url(url).post(create(JSON_MEDIA_TYPE, toJson(payload)));
  }

  private Request.Builder deleteRequest(HttpUrl url) {
    return new Request.Builder().url(url).delete();
  }

  private Request.Builder headRequest(HttpUrl url) {
    return new Request.Builder().url(url).head();
  }

  private Request.Builder getRequest(HttpUrl url) {
    return new Request.Builder().url(url).get();
  }

  private <T> T executeAndGet(Request.Builder request, Function<String, T> contentParser) {
    return execute(request.build(), response -> {
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
        final String message;
        if (res.code() >= 500) {
          message = res.message();
        } else {
          message = res.body() != null ? res.body().string() : res.message();
        }
        throw new ApiException(res.code(), message);
      }
      return handler.apply(res);
    } catch (IOException e) {
      throw new ClientException(e);
    }
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
