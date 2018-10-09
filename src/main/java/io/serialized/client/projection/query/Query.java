package io.serialized.client.projection.query;

import okhttp3.HttpUrl;

import java.util.Optional;

public interface Query {

  /**
   * Build the full URL for the projection query
   *
   * @param rootUrl the root API url (normally Serialized public API)
   * @return the full query url
   */
  HttpUrl constructUrl(HttpUrl rootUrl);

  /**
   * Class that the data in the projection result should be serialized to.
   *
   * @return the response class
   */
  Optional<Class> responseClass();
}
