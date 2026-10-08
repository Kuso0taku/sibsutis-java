package task8;

import src.Course;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// обычная реализация: умеет и читать, и писать
public class InMemoryCourseRepository implements CourseRepository {
  private final Map<Long, Course> storage = new HashMap<>();

  @Override
  public Optional<Course> findById(long id) {
    return Optional.ofNullable(storage.get(id));
  }

  @Override
  public void save(Course course) {
    storage.put(course.id(), course);
  }
}
