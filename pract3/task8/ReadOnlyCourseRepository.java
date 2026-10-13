package task8;

import src.Course;
import java.util.Optional;

// read-only: реализует только чтение, save вызвать просто нельзя —
// контракт подтипа не обещает невозможного (LSP восстановлен)
public class ReadOnlyCourseRepository implements CourseReadRepository {
  private final CourseReadRepository delegate;

  public ReadOnlyCourseRepository(CourseReadRepository delegate) {
    this.delegate = delegate;
  }

  @Override
  public Optional<Course> findById(long id) {
    return delegate.findById(id);
  }
}
