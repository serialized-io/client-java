package io.serialized.client.feed;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class GetFeedRequestTest {

  @Test
  void testWithPartitioning() {
    assertThrows(IllegalArgumentException.class, () -> new GetFeedRequest.Builder().withPartitioning(-1, -1));
    assertThrows(IllegalArgumentException.class, () -> new GetFeedRequest.Builder().withPartitioning(0, -1));
    assertThrows(IllegalArgumentException.class, () -> new GetFeedRequest.Builder().withPartitioning(1, -1));
    assertThrows(IllegalArgumentException.class, () -> new GetFeedRequest.Builder().withPartitioning(1, -1));
    assertThrows(IllegalArgumentException.class, () -> new GetFeedRequest.Builder().withPartitioning(2, -1));
  }

}
