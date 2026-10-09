import org.junit.jupiter.api.Test;
import src.Course;
import src.CourseId;
import src.Page;
import src.Result;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {
  @Test
  void ok_carriesTypedValue() {
    // Arrange: конкретизация Result<Course>
    Course course = new Course(new CourseId(1), "Java", 16);

    // Act
    Result<Course> result = Result.ok(course);

    // Assert
    assertTrue(result.isOk());
    assertSame(course, result.value());
    assertTrue(result.toOptional().isPresent());
  }

  @Test
  void error_carriesMessageAndHasNoValue() {
    // Arrange: конкретизация Result<CourseId>
    Result<CourseId> result = Result.error("course not found");

    // Act + Assert
    assertTrue(result.isError());
    assertEquals("course not found", result.error());
    assertThrows(IllegalStateException.class, result::value);
    assertTrue(result.toOptional().isEmpty());
  }

  @Test
  void factories_rejectMissingParts() {
    // Arrange + Act + Assert: инварианты проверяются фабриками
    assertThrows(NullPointerException.class, () -> Result.ok(null));
    assertThrows(IllegalArgumentException.class, () -> Result.error("  "));
  }

  @Test
  void map_transformsOnlyOkBranch() {
    // Arrange
    Result<Course> ok = Result.ok(new Course(new CourseId(1), "Java", 16));
    Result<Course> error = Result.error("boom");

    // Act
    Result<String> title = ok.map(c -> c.title().toUpperCase());
    Result<String> stillError = error.map(c -> c.title().toUpperCase());

    // Assert
    assertEquals("JAVA", title.value());
    assertTrue(stillError.isError());
    assertEquals("boom", stillError.error());
  }

  @Test
  void flatMap_doesNotWrapTwice() {
    // Arrange
    Result<Course> source = Result.ok(new Course(new CourseId(1), "Java", 16));

    // Act: функция сама возвращает Result
    Result<Integer> hours = source.flatMap(c -> Result.ok(c.durationHours()));

    // Assert
    assertEquals(16, hours.value());
  }

  @Test
  void equals_comparesStateNotIdentity() {
    // Arrange
    Result<Integer> a = Result.ok(5);
    Result<Integer> b = Result.ok(5);

    // Act + Assert
    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertNotEquals(a, Result.error("5"));
  }

  @Test
  void page_isAnImmutableView() {
    // Arrange
    List<String> raw = new java.util.ArrayList<>(List.of("Java", "Kotlin", "SQL"));

    // Act
    Page<String> page = new Page<>(raw, 0, 2, 5);
    raw.add("Go"); // изменение источника не должно просочиться

    // Assert: список скопирован в конструкторе
    assertEquals(List.of("Java", "Kotlin", "SQL"), page.items());
    assertThrows(UnsupportedOperationException.class, () -> page.items().add("Rust"));
    assertEquals(3, page.totalPages());
    assertTrue(page.hasNext());
  }

  // Компиляторная защита: это не соберется, поэтому оставлено комментарием.
  // Result<Course> courses = Result.ok(new Course(...));
  // Result<CourseId> ids = courses; // incompatible types: Result<Course> -> Result<CourseId>
}
