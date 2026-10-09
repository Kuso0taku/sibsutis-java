import org.junit.jupiter.api.Test;
import src.CourseCode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Проверяем и результат, и причину: set убирает дубликаты только потому,
// что CourseCode реализует equals/hashCode.
class DeduplicatorTest {
  private static CourseCode c(String raw) {
    return CourseCode.parse(raw);
  }

  @Test
  void unique_keepsOrderOfFirstAppearance() {
    // Arrange
    List<CourseCode> codes = List.of(
        c("java-101"), c("sql-301"), c("JAVA101"), c("kotlin-201"), c("SQL301"));

    // Act
    List<CourseCode> unique = Deduplicator.unique(codes);

    // Assert: порядок первого появления сохранен
    assertEquals(List.of(c("JAVA101"), c("SQL301"), c("KOTLIN201")), unique);
  }

  @Test
  void unique_collapsesSameNormalizedCodeFromDifferentSources() {
    // Arrange: три записи, которые нормализуются в один код
    List<CourseCode> codes = List.of(
        c("java-101"), c(" JAVA_101 "), c("Java 101"));

    // Act
    List<CourseCode> unique = Deduplicator.unique(codes);

    // Assert
    assertEquals(1, unique.size());
    assertEquals(c("JAVA101"), unique.get(0));
  }

  @Test
  void naiveVersion_givesSameResult() {
    // Arrange
    List<CourseCode> codes = List.of(
        c("java-101"), c("sql-301"), c("JAVA101"));

    // Act
    List<CourseCode> fromSet = Deduplicator.unique(codes);
    List<CourseCode> fromList = Deduplicator.uniqueNaive(codes);

    // Assert: результат одинаковый, разница только в сложности
    assertEquals(fromList, fromSet);
  }

  @Test
  void withoutEqualsAndHashCode_setCannotTellCopiesApart() {
    // Arrange: класс-близнец без equals/hashCode
    List<PlainCode> codes = List.of(
        new PlainCode("JAVA101"), new PlainCode("JAVA101"), new PlainCode("JAVA101"));

    // Act
    long distinct = codes.stream().distinct().count();

    // Assert: identity-сравнение оставляет все три "копии"
    assertEquals(3, distinct, "без equals/hashCode это три разных объекта");
  }

  @Test
  void unique_rejectsNull() {
    // Arrange
    List<CourseCode> codes = new java.util.ArrayList<>();
    codes.add(c("java-101"));
    codes.add(null);

    // Act + Assert: null - ошибка вызывающего, а не элемент
    assertThrows(NullPointerException.class, () -> Deduplicator.unique(codes));
  }

  static final class PlainCode {
    private final String value;

    PlainCode(String value) {
      this.value = value;
    }
  }
}
