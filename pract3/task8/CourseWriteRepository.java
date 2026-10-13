package task8;

import src.Course;

// только запись: отдельный контракт
public interface CourseWriteRepository {
  void save(Course course);
}
