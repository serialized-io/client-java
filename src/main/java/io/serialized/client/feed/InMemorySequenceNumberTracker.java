package io.serialized.client.feed;

import org.apache.commons.lang3.Validate;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Default, in-memory, implementation of the sequence number tracker.
 * Will keep the state of which feed entries have been consumed.
 */
public class InMemorySequenceNumberTracker implements SequenceNumberTracker {

  private final AtomicLong sequenceNumber;

  public InMemorySequenceNumberTracker() {
    this.sequenceNumber = new AtomicLong(0);
  }

  public InMemorySequenceNumberTracker(long initialValue) {
    this.sequenceNumber = new AtomicLong(initialValue);
  }

  @Override
  public long lastConsumedSequenceNumber() {
    return sequenceNumber.longValue();
  }

  @Override
  public void updateLastConsumedSequenceNumber(long sequenceNumber) {
    Validate.isTrue(sequenceNumber >= 0, "Last consumed sequence number cannot be negative!");
    this.sequenceNumber.set(sequenceNumber);
  }

  @Override
  public void reset() {
    sequenceNumber.set(0);
  }

}
