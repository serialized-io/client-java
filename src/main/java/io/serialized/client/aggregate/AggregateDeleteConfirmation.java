package io.serialized.client.aggregate;

import io.serialized.client.SerializedOkHttpClient;
import okhttp3.HttpUrl;

import java.util.UUID;

public class AggregateDeleteConfirmation {

  private final SerializedOkHttpClient client;
  private final HttpUrl url;
  private final UUID tenantId;

  AggregateDeleteConfirmation(SerializedOkHttpClient client, HttpUrl url) {
    this(client, url, null);
  }

  AggregateDeleteConfirmation(SerializedOkHttpClient client, HttpUrl url, UUID tenantId) {
    this.client = client;
    this.url = url;
    this.tenantId = tenantId;
  }

  /**
   * Confirm and perform the actual delete.
   */
  public void confirm() {
    if (tenantId == null) {
      client.delete(url);
    } else {
      client.delete(url, tenantId);
    }
  }

}
