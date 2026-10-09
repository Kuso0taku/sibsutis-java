import org.junit.jupiter.api.Test;
import src.CollectionCopies;
import src.Course;
import src.CourseId;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PecsTest {
  @Test
  void copy_fromSubtypeListToSupertypeList() {
    // Arrange: producer List<Integer>, consumer List<Number>
    List<Integer> integers = List.of(1, 2, 3);
    List<Number> numbers = new ArrayList<>();

    // Act
    CollectionCopies.copy(integers, numbers);

    // Assert
    assertEquals(List.of(1, 2, 3), numbers);
  }

  @Test
  void copy_fromCourseListToObjectList() {
    // Arrange: конкретный тип -> более общий
    List<Course> courses = List.of(new Course(new CourseId(1), "Java", 16));
    List<Object> objects = new ArrayList<>();

    // Act
    CollectionCopies.copy(courses, objects);

    // Assert
    assertEquals(1, objects.size());
    assertSame(courses.get(0), objects.get(0));
  }

  @Test
  void superList_allowsWritingEvenIfConcreteTypeUnknown() {
    // Arrange: List<? super Course> и List<? super Integer>
    List<Object> storage = new ArrayList<>();
    List<? super Course> consumer = storage;

    // Act: в consumer можно класть Course, тип элемента consumer - предок Course
    consumer.add(new Course(new CourseId(2), "Kotlin", 24));
    consumer.add(new Course(new CourseId(3), "SQL", 8));

    // Assert
    assertEquals(2, storage.size());
  }

  @Test
  void extendsList_allowsReadingAsTopTypeOnly() {
    // Arrange: List<? extends Course>
    List<? extends Course> producer =
        List.of(new Course(new CourseId(4), "Go", 12));

    // Act: прочитать можно гарантированно как Course
    Course first = producer.get(0);

    // Assert
    assertEquals("Go", first.title());
    // producer.add(new Course(...)) не компилируется: тип элемента - какой-то
    // неизвестный подтип Course, и добавить конкретный Course нельзя.
  }

  // Компиляторная защита - оставлено комментарием:
  // List<? extends Course> producer = new ArrayList<Course>();
  // producer.add(new Course(new CourseId(9), "Rust", 10)); // compile error
}
