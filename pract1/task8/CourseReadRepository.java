package task8;

import src.Course;
import java.util.Optional;

// только чтение: контракт не обещает запись
public interface CourseReadRepository {
  Optional<Course> findById(long id);
}
