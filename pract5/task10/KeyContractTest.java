import org.junit.jupiter.api.Test;
import src.Course;
import src.CourseId;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class KeyContractTest {
  @Test
  void changingNonEqualityField_keepsLookupWorking() {
    // Arrange
    Course course = new Course(new CourseId(1), "Java", 16);
    Set<Course> set = new HashSet<>();
    set.add(course);
    Map<Course, String> map = new HashMap<>();
    map.put(course, "spring-2026");

    // Act: popularity не входит в equals/hashCode
    course.setPopularity(999);

    // Assert: объект по-прежнему находится
    assertTrue(set.contains(course));
    assertEquals("spring-2026", map.get(course));
  }

  @Test
  void equalCourses_shareHashCode() {
    // Arrange: два разных объекта с одним id
    Course a = new Course(new CourseId(7), "Java", 16);
    Course b = new Course(new CourseId(7), "Java Advanced", 32);

    // Act + Assert: equals по id => одинаковый hashCode
    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertEquals(1, new HashSet<>(Set.of(a, b)).size());
  }

  @Test
  void mutableHashField_breaksSetLookup() {
    // Arrange
    BadCourse course = new BadCourse(1, 10);
    Set<BadCourse> set = new HashSet<>();
    set.add(course);

    // Act
    course.setPopularity(999);

    // Assert: объект остался в set, но найти его нельзя
    assertEquals(1, set.size(), "element is still physically inside");
    assertFalse(set.contains(course), "but hash no longer matches its bucket");
  }

  @Test
  void mutableHashField_breaksMapLookup() {
    // Arrange
    BadCourse course = new BadCourse(1, 10);
    Map<BadCourse, String> map = new HashMap<>();
    map.put(course, "spring-2026");

    // Act
    course.setPopularity(999);

    // Assert
    assertNull(map.get(course));
    assertEquals(1, map.size(), "entry is still inside, just unreachable");
  }

  @Test
  void keyShouldBeImmutable() {
    // Arrange: корректный ключ - неизменяемый CourseId
    CourseId id = new CourseId(3);
    Map<CourseId, String> map = new HashMap<>();
    map.put(id, "Kotlin");

    // Act: значение меняется, ключ - нет
    map.put(id, "Kotlin v2");

    // Assert
    assertEquals("Kotlin v2", map.get(new CourseId(3)));
  }
}
