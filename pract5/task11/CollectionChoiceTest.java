import org.junit.jupiter.api.Test;
import src.CourseId;

import java.util.List;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.*;

class CollectionChoiceTest {
  @Test
  void leaderboard_ordersByScoreDescending() {
    // Arrange
    Leaderboard board = new Leaderboard();

    // Act
    board.score("Ann", 90);
    board.score("Bob", 70);
    board.score("Cid", 90);
    board.score("Dan", 50);

    // Assert: тот же счет - в порядке добавления
    assertEquals(List.of("Ann", "Cid", "Bob", "Dan"), board.top(4));
    assertEquals(90, board.bestScore());
    assertEquals(List.of("Ann", "Cid"), board.top(2));
  }

  @Test
  void range_returnsOnlyIdsInsideIntervalInOrder() {
    // Arrange
    RangeIndex index = new RangeIndex();
    index.put(new CourseId(10), "A");
    index.put(new CourseId(20), "B");
    index.put(new CourseId(30), "C");
    index.put(new CourseId(40), "D");

    // Act
    SortedMap<CourseId, String> window = index.range(15, 35);

    // Assert: O(log n + k), границы включительно, порядок по ключу
    assertEquals(List.of(new CourseId(20), new CourseId(30)),
        List.copyOf(window.keySet()));
  }

  @Test
  void range_withInvertedBoundsIsRejected() {
    // Arrange
    RangeIndex index = new RangeIndex();

    // Act + Assert
    assertThrows(IllegalArgumentException.class, () -> index.range(30, 10));
  }

  @Test
  void leaderboard_topMoreThanPresent_returnsEveryone() {
    // Arrange
    Leaderboard board = new Leaderboard();
    board.score("Ann", 10);

    // Act + Assert: просим больше, чем есть - не падаем
    assertEquals(List.of("Ann"), board.top(99));
  }
}
