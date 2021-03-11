package io.serialized.client.feed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InMemorySequenceNumberTrackerTest {

  private SequenceNumberTracker sequenceNumberTracker;

  @BeforeEach
  void setUp() {
    sequenceNumberTracker = new InMemorySequenceNumberTracker();
  }

  @Test
  public void testSequenceNumber() {
    assertThat(sequenceNumberTracker.lastConsumedSequenceNumber()).isEqualTo(0);

    sequenceNumberTracker.updateLastConsumedSequenceNumber(1);
    assertThat(sequenceNumberTracker.lastConsumedSequenceNumber()).isEqualTo(1);

    sequenceNumberTracker.updateLastConsumedSequenceNumber(100_000_000);
    assertThat(sequenceNumberTracker.lastConsumedSequenceNumber()).isEqualTo(100_000_000);

    sequenceNumberTracker.reset();
    assertThat(sequenceNumberTracker.lastConsumedSequenceNumber()).isEqualTo(0);
  }

  @Test
  public void testSequenceNumberMustBePositive() {
    Throwable exception = assertThrows(IllegalArgumentException.class,
        () -> sequenceNumberTracker.updateLastConsumedSequenceNumber(-1));

    assertThat(exception.getMessage()).isEqualTo("Last consumed sequence number cannot be negative!");
  }

  @Test
  public void testSequenceNumberMustNotBeEqualToCurrent() {
    sequenceNumberTracker.updateLastConsumedSequenceNumber(10);

    Throwable exception = assertThrows(IllegalArgumentException.class,
        () -> sequenceNumberTracker.updateLastConsumedSequenceNumber(10));

    assertThat(exception.getMessage()).isEqualTo("Last consumed sequence number must be greater than current!");
  }

  @Test
  public void testSequenceNumberMustBeGreaterThanCurrent() {
    sequenceNumberTracker.updateLastConsumedSequenceNumber(10);

    Throwable exception = assertThrows(IllegalArgumentException.class,
        () -> sequenceNumberTracker.updateLastConsumedSequenceNumber(9));

    assertThat(exception.getMessage()).isEqualTo("Last consumed sequence number must be greater than current!");
  }

}
