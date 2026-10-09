import org.junit.jupiter.api.Test;
import src.CourseCode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// ArrayList как List: проверяем главное - порядок и позиции.
// Имя теста описывает наблюдаемое поведение, а не реализацию.
class CourseRegistryTest {
  private static CourseCode c(String raw) {
    return CourseCode.parse(raw);
  }

  @Test
  void register_keepsInsertionOrder() {
    // Arrange
    CourseRegistry registry = new CourseRegistry();

    // Act
    registry.register(c("java-101"));
    registry.register(c("KOTLIN-201"));
    registry.register(c("sql-301"));

    // Assert: List не переупорядочивает элементы
    assertEquals(List.of(c("JAVA101"), c("KOTLIN201"), c("SQL301")),
        registry.sequence());
  }

  @Test
  void insertAt_putsElementInTheMiddleAndShiftsTail() {
    // Arrange
    CourseRegistry registry = new CourseRegistry();
    registry.register(c("java-101"));
    registry.register(c("sql-301"));

    // Act
    registry.insertAt(1, c("kotlin-201"));

    // Assert
    assertEquals(c("KOTLIN201"), registry.at(1));
    assertEquals(List.of(c("JAVA101"), c("KOTLIN201"), c("SQL301")),
        registry.sequence());
  }

  @Test
  void remove_removesFirstOccurrenceOnly() {
    // Arrange: один и тот же код зарегистрирован дважды
    CourseRegistry registry = new CourseRegistry();
    registry.register(c("java-101"));
    registry.register(c("sql-301"));
    registry.register(c("JAVA101"));

    // Act
    boolean removed = registry.remove(c("java-101"));

    // Assert: удалилась первая позиция, вторая осталась
    assertTrue(removed);
    assertEquals(List.of(c("SQL301"), c("JAVA101")), registry.sequence());
  }

  @Test
  void sequence_isAnImmutableCopy() {
    // Arrange
    CourseRegistry registry = new CourseRegistry();
    registry.register(c("java-101"));

    // Act
    List<CourseCode> snapshot = registry.sequence();

    // Assert: наружу не утекает внутренний изменяемый список
    assertThrows(UnsupportedOperationException.class,
        () -> snapshot.add(c("sql-301")));
    assertEquals(1, registry.size());
  }

  @Test
  void duplicates_reportsInOrderOfFirstRepeat() {
    // Arrange
    List<CourseCode> codes = List.of(
        c("java-101"), c("sql-301"), c("JAVA101"), c("kotlin-201"), c("SQL301"));

    // Act
    List<CourseCode> duplicates = DuplicateFinder.duplicates(codes);

    // Assert: "java-101" повторился раньше "sql-301"
    assertEquals(List.of(c("JAVA101"), c("SQL301")), duplicates);
  }
}
