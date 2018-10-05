package io.serialized.client.projection;

import okhttp3.HttpUrl;

import java.util.Optional;

public interface Query {
  HttpUrl constructUrl(HttpUrl rootUrl);

  Optional<Class> responseClass();
}
