import org.junit.jupiter.api.Test;
import src.Course;
import src.CourseId;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

// Проверяем, что повторный id не затирает данные молча,
// и что computeIfAbsent вызывает фабрику только при промахе.
class CourseIndexTest {
  private static Course course(long id, String title) {
    return new Course(new CourseId(id), title, 16);
  }

  @Test
  void add_storesCourseAndFindsItById() {
    // Arrange
    CourseIndex index = new CourseIndex();

    // Act
    index.add(course(1, "Java"));

    // Assert
    Optional<Course> found = index.find(new CourseId(1));
    assertTrue(found.isPresent());
    assertEquals("Java", found.get().title());
    assertTrue(index.contains(new CourseId(1)));
  }

  @Test
  void find_returnsEmptyForUnknownId() {
    // Arrange
    CourseIndex index = new CourseIndex();
    index.add(course(1, "Java"));

    // Act + Assert
    assertTrue(index.find(new CourseId(99)).isEmpty());
    assertFalse(index.contains(new CourseId(99)));
  }

  @Test
  void add_duplicateIdThrowsInsteadOfSilentOverwrite() {
    // Arrange
    CourseIndex index = new CourseIndex();
    index.add(course(1, "Java"));

    // Act: тот же id - попытка молча затереть старое значение
    IllegalStateException e = assertThrows(
        IllegalStateException.class,
        () -> index.add(course(1, "Java v2")));

    // Assert: индекс не изменился
    assertEquals("duplicate id: CourseId(1)", e.getMessage());
    assertEquals("Java", index.find(new CourseId(1)).orElseThrow().title());
    assertEquals(1, index.size());
  }

  @Test
  void replace_isExplicitAndOverwrites() {
    // Arrange
    CourseIndex index = new CourseIndex();
    index.add(course(1, "Java"));

    // Act
    index.replace(course(1, "Java v2"));

    // Assert
    assertEquals("Java v2", index.find(new CourseId(1)).orElseThrow().title());
    assertEquals(1, index.size());
  }

  @Test
  void findOrAdd_createsOnlyOnMiss() {
    // Arrange
    CourseIndex index = new CourseIndex();
    AtomicInteger calls = new AtomicInteger();

    // Act: первый вызов создает, второй переиспользует
    Course first = index.findOrAdd(new CourseId(1),
        () -> { calls.incrementAndGet(); return course(1, "Java"); });
    Course second = index.findOrAdd(new CourseId(1),
        () -> { calls.incrementAndGet(); return course(1, "Other"); });

    // Assert: фабрика вызвана один раз, второй раз вернулся тот же объект
    assertEquals(1, calls.get());
    assertSame(first, second);
    assertEquals("Java", second.title());
  }
}
