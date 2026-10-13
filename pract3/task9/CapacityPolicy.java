package task9;

import src.Course;
import src.Student;

// политика по вместимости
public class CapacityPolicy implements EnrollmentPolicy {
  @Override
  public boolean allows(Student student, Course course) {
    return course.seatsLeft() > 0;
  }
}
