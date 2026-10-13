package task9;

import src.Course;
import src.Student;

// открытая стратегия: новая политика — новый класс, сервис не меняется
public interface EnrollmentPolicy {
  boolean allows(Student student, Course course);
}
