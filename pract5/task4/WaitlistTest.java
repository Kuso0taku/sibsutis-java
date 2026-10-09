import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

// FIFO проверяем явно: порядок извлечения совпадает с порядком постановки.
// Пустая очередь - отдельный значимый случай, а не "должно быть пусто".
class WaitlistTest {
  @Test
  void take_returnsStudentsInFifoOrder() {
    // Arrange
    Waitlist waitlist = new Waitlist();
    waitlist.enqueue(100);
    waitlist.enqueue(200);
    waitlist.enqueue(300);

    // Act + Assert
    assertEquals(100L, waitlist.take().orElseThrow());
    assertEquals(200L, waitlist.take().orElseThrow());
    assertEquals(300L, waitlist.take().orElseThrow());
    assertEquals(0, waitlist.size());
  }

  @Test
  void next_peeksWithoutRemoving() {
    // Arrange
    Waitlist waitlist = new Waitlist();
    waitlist.enqueue(100);
    waitlist.enqueue(200);

    // Act
    long first = waitlist.next().orElseThrow();
    long again = waitlist.next().orElseThrow();

    // Assert: peek не меняет очередь
    assertEquals(100, first);
    assertEquals(100, again);
    assertEquals(2, waitlist.size());
  }

  @Test
  void peekOnEmptyQueue_returnsNullNotException() {
    // Arrange
    Waitlist waitlist = new Waitlist();

    // Act + Assert
    assertNull(waitlist.nextOrNull());
    assertTrue(waitlist.next().isEmpty());
  }

  @Test
  void pollOnEmptyQueue_returnsNullNotException() {
    // Arrange
    Waitlist waitlist = new Waitlist();

    // Act + Assert
    assertNull(waitlist.takeOrNull());
    assertTrue(waitlist.take().isEmpty());
  }

  @Test
  void elementAndRemove_throwOnEmptyQueue() {
    // Arrange
    Waitlist waitlist = new Waitlist();

    // Act + Assert: "строгие" версии сигналят об ошибке, а не возвращают null
    assertThrows(NoSuchElementException.class, waitlist::nextOrThrow);
    assertThrows(NoSuchElementException.class, waitlist::takeOrThrow);
  }

  @Test
  void offerAndAdd_insertTheSameWay() {
    // Arrange
    Waitlist waitlist = new Waitlist();

    // Act
    assertTrue(waitlist.enqueue(100));
    waitlist.enqueueOrThrow(200);

    // Assert
    assertEquals(100, waitlist.takeOrThrow());
    assertEquals(200, waitlist.takeOrThrow());
  }
}
