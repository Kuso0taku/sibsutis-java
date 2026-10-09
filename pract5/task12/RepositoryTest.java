import org.junit.jupiter.api.Test;
import src.Course;
import src.CourseId;
import src.InMemoryRepository;
import src.Repository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryTest {
  private static Repository<CourseId, Course> repo() {
    return new InMemoryRepository<>(c -> c.id());
  }

  private static Course course(CourseId id, String title) {
    return new Course(id, title, 16);
  }

  @Test
  void saveThenFindById_returnsSameEntity() {
    // Arrange
    Repository<CourseId, Course> repository = repo();
    Course course = course(new CourseId(1), "Java");

    // Act
    repository.save(course);
    Optional<Course> found = repository.findById(new CourseId(1));

    // Assert
    assertTrue(found.isPresent());
    assertSame(course, found.get());
  }

  @Test
  void findById_unknownId_returnsEmpty() {
    // Arrange
    Repository<CourseId, Course> repository = repo();

    // Act + Assert
    assertTrue(repository.findById(new CourseId(99)).isEmpty());
  }

  @Test
  void repeatedSaveWithSameId_replacesWithoutGrowing() {
    // Arrange: семантика upsert
    Repository<CourseId, Course> repository = repo();
    repository.save(course(new CourseId(1), "Java"));
    repository.save(course(new CourseId(1), "Java v2"));

    // Act
    Optional<Course> found = repository.findById(new CourseId(1));

    // Assert: новое значение, размер не вырос
    assertEquals("Java v2", found.orElseThrow().title());
    assertEquals(1, repository.findAll().size());
  }

  @Test
  void findAll_returnsImmutableSnapshotOfCurrentState() {
    // Arrange
    Repository<CourseId, Course> repository = repo();
    repository.save(course(new CourseId(1), "Java"));

    // Act
    List<Course> snapshot = repository.findAll();
    repository.save(course(new CourseId(2), "Kotlin"));

    // Assert: снимок не увидел второй save и его нельзя изменить
    assertEquals(1, snapshot.size());
    assertThrows(UnsupportedOperationException.class, () -> snapshot.add(null));
  }

  @Test
  void deleteById_removesOnlyThatEntity() {
    // Arrange
    Repository<CourseId, Course> repository = repo();
    repository.save(course(new CourseId(1), "Java"));
    repository.save(course(new CourseId(2), "Kotlin"));

    // Act
    boolean removed = repository.deleteById(new CourseId(1));

    // Assert
    assertTrue(removed);
    assertTrue(repository.findById(new CourseId(2)).isPresent());
    assertEquals(1, repository.findAll().size());
    assertFalse(repository.deleteById(new CourseId(1)), "second delete is a miss");
  }

  @Test
  void nullIsRejectedEverywhere() {
    // Arrange
    Repository<CourseId, Course> repository = repo();

    // Act + Assert: null - ошибка программиста, а не данные
    assertThrows(NullPointerException.class, () -> repository.save(null));
    assertThrows(NullPointerException.class, () -> repository.findById(null));
    assertThrows(NullPointerException.class, () -> repository.deleteById(null));
  }
}