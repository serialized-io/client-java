package io.serialized.client.aggregate;

import io.serialized.client.SerializedOkHttpClient;
import okhttp3.HttpUrl;

public class AggregateDeleteConfirmation {

  private final SerializedOkHttpClient client;
  private final HttpUrl url;

  AggregateDeleteConfirmation(SerializedOkHttpClient client, HttpUrl url) {
    this.client = client;
    this.url = url;
  }

  public void confirm() {
    client.delete(url);
  }

}
