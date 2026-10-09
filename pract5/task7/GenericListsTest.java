import org.junit.jupiter.api.Test;
import src.GenericLists;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class GenericListsTest {
  @Test
  void firstOrThrow_returnsHeadForAnyType() {
    // Arrange: один и тот же метод работает и для String, и для Integer
    List<String> strings = List.of("a", "b");
    List<Integer> numbers = List.of(1, 2, 3);

    // Act + Assert: параметр типа выводится на месте вызова
    assertEquals("a", GenericLists.firstOrThrow(strings));
    assertEquals(1, GenericLists.firstOrThrow(numbers));
  }

  @Test
  void firstOrThrow_rejectsEmptyList() {
    // Arrange
    List<String> empty = List.of();

    // Act + Assert
    assertThrows(NoSuchElementException.class, () -> GenericLists.firstOrThrow(empty));
  }

  @Test
  void swap_exchangesElementsInPlace() {
    // Arrange
    List<String> list = new ArrayList<>(List.of("a", "b", "c"));

    // Act
    GenericLists.swap(list, 0, 2);

    // Assert
    assertEquals(List.of("c", "b", "a"), list);
  }

  @Test
  void swap_rejectsOutOfRangeIndex() {
    // Arrange
    List<String> list = new ArrayList<>(List.of("a"));

    // Act + Assert
    assertThrows(IndexOutOfBoundsException.class, () -> GenericLists.swap(list, 0, 1));
  }

  @Test
  void indexBy_buildsMapByKey() {
    // Arrange
    List<String> courses = List.of("Java", "Kotlin", "SQL");

    // Act: ключ - длина названия
    Map<Integer, String> byLength =
        GenericLists.indexBy(courses, String::length);

    // Assert
    assertEquals("Java", byLength.get(4));
    assertEquals("SQL", byLength.get(3));
    assertEquals(3, byLength.size());
  }

  @Test
  void indexBy_conflictThrowsInsteadOfSilentOverwrite() {
    // Arrange: "Java" и "Rust" дают одинаковую длину
    List<String> courses = List.of("Java", "Kotlin", "Rust");

    // Act
    IllegalStateException e = assertThrows(
        IllegalStateException.class,
        () -> GenericLists.indexBy(courses, String::length));

    // Assert: видно, какой ключ и какие значения столкнулись
    assertTrue(e.getMessage().contains("duplicate key 4"));
  }

  @Test
  void indexBy_withMerge_resolvesConflictExplicitly() {
    // Arrange
    List<String> courses = List.of("Java", "Kotlin", "Rust");

    // Act: стратегия - оставить более длинное название
    Map<Integer, String> resolved = GenericLists.indexBy(
        courses, String::length, (a, b) -> a.length() >= b.length() ? a : b);

    // Assert
    assertEquals("Kotlin", resolved.get(6));
    assertEquals(2, resolved.size()); // длины 4 и 6
  }
}
