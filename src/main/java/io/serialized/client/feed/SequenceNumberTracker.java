package io.serialized.client.feed;

public interface SequenceNumberTracker {

  /**
   * @return The last consumed sequence number.
   */
  long lastConsumedSequenceNumber();

  /**
   * Updates the last consumed sequence number.
   *
   * @param sequenceNumber The updated number
   */
  void updateLastConsumedSequenceNumber(long sequenceNumber);

  /**
   * Resets the number to enable feed subscription to replay from the beginning.
   */
  void reset();

}
