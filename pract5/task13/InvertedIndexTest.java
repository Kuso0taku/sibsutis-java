import org.junit.jupiter.api.Test;
import src.Course;
import src.CourseId;
import src.Tag;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class InvertedIndexTest {
  private static Course course(long id, String title, String... tagNames) {
    Course course = new Course(new CourseId(id), title, 16);
    for (String name : tagNames) {
      course.addTag(Tag.of(name));
    }
    return course;
  }

  @Test
  void add_indexesCourseUnderEveryTag() {
    // Arrange
    InvertedIndex index = new InvertedIndex();
    Course java = course(1, "Java", "backend", "jvm");

    // Act
    index.add(java);

    // Assert
    assertEquals(2, index.tagCount());
    assertEquals(Set.of(new CourseId(1)),
        index.coursesWithAll(Set.of(Tag.of("backend"))));
    assertEquals(Set.of(new CourseId(1)),
        index.coursesWithAll(Set.of(Tag.of("JVM"))));
  }

  @Test
  void intersection_requiresAllTags() {
    // Arrange
    InvertedIndex index = new InvertedIndex();
    index.add(course(1, "Java", "backend", "jvm"));
    index.add(course(2, "Kotlin", "backend", "android"));
    index.add(course(3, "SQL", "data"));

    // Act
    Set<CourseId> backend = index.coursesWithAll(Set.of(Tag.of("backend")));
    Set<CourseId> backendAndJvm =
        index.coursesWithAll(Set.of(Tag.of("backend"), Tag.of("jvm")));

    // Assert
    assertEquals(Set.of(new CourseId(1), new CourseId(2)), backend);
    assertEquals(Set.of(new CourseId(1)), backendAndJvm);
  }

  @Test
  void intersection_withUnknownTag_returnsEmpty() {
    // Arrange
    InvertedIndex index = new InvertedIndex();
    index.add(course(1, "Java", "backend"));

    // Act
    Set<CourseId> result = index.coursesWithAll(Set.of(Tag.of("backend"), Tag.of("ai")));

    // Assert
    assertTrue(result.isEmpty());
  }

  @Test
  void union_returnsCoursesHavingAnyTag() {
    // Arrange
    InvertedIndex index = new InvertedIndex();
    index.add(course(1, "Java", "backend", "jvm"));
    index.add(course(2, "Kotlin", "android"));
    index.add(course(3, "SQL", "data"));

    // Act
    Set<CourseId> result = index.coursesWithAny(Set.of(Tag.of("jvm"), Tag.of("data")));

    // Assert
    assertEquals(Set.of(new CourseId(1), new CourseId(3)), result);
  }

  @Test
  void remove_stripsIdFromAllTags_andDropsEmptySets() {
    // Arrange
    InvertedIndex index = new InvertedIndex();
    Course java = course(1, "Java", "backend", "jvm");
    index.add(java);
    index.add(course(2, "Kotlin", "backend"));

    // Act: удаляем курс 1 из обоих его тегов
    index.remove(new CourseId(1), Set.of(Tag.of("backend"), Tag.of("jvm")));

    // Assert: у "jvm" не осталось курсов - set пропал из индекса
    assertEquals(1, index.tagCount(), "empty jvm set must be removed");
    assertEquals(Set.of(new CourseId(2)),
        index.coursesWithAll(Set.of(Tag.of("backend"))));
    assertTrue(index.coursesWithAll(Set.of(Tag.of("jvm"))).isEmpty());
  }

  @Test
  void remove_withTagNotInIndex_stillWorks() {
    // Arrange
    InvertedIndex index = new InvertedIndex();
    index.add(course(1, "Java", "backend"));

    // Act: удаляем курс с тегом, которого в индексе нет
    index.remove(new CourseId(1), Set.of(Tag.of("missing")));

    // Assert: ничего не упало, данные целы
    assertEquals(1, index.tagCount());
    assertEquals(Set.of(new CourseId(1)),
        index.coursesWithAll(Set.of(Tag.of("backend"))));
  }
}