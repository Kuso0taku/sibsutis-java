import org.junit.jupiter.api.Test;
import src.CourseCode;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SafeRemovalTest {
  private static CourseCode c(String raw) {
    return CourseCode.parse(raw);
  }

  private static List<CourseCode> sample() {
    return new ArrayList<>(List.of(c("java-101"), c("sql-301"), c("JAVA101")));
  }

  @Test
  void enhancedForRemoval_throwsConcurrentModificationException() {
    // Arrange
    List<CourseCode> codes = sample();

    // Act + Assert: прямое удаление во время обхода ломает итератор
    assertThrows(ConcurrentModificationException.class, () -> {
      for (CourseCode code : codes) {
        if (code.equals(c("JAVA101"))) {
          codes.remove(code);
        }
      }
    });
  }

  @Test
  void iteratorRemoval_deletesAllMatchesWithoutException() {
    // Arrange: код java-101 встречается дважды (с учетом нормализации)
    List<CourseCode> codes = sample();

    // Act
    SafeRemoval.removeWithIterator(codes, c("JAVA101"));

    // Assert
    assertEquals(List.of(c("SQL301")), codes);
  }

  @Test
  void removeIf_deletesAllMatchesWithoutException() {
    // Arrange
    List<CourseCode> codes = sample();

    // Act
    SafeRemoval.removeWithRemoveIf(codes, c("JAVA101"));

    // Assert
    assertEquals(List.of(c("SQL301")), codes);
  }

  @Test
  void removalOfAbsentElement_changesNothing() {
    // Arrange
    List<CourseCode> codes = sample();

    // Act
    SafeRemoval.removeWithRemoveIf(codes, c("GO-401"));

    // Assert
    assertEquals(sample(), codes);
  }
}
