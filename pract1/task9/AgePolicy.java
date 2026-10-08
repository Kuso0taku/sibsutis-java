package task9;

import src.Course;
import src.Student;

// политика по возрасту
public class AgePolicy implements EnrollmentPolicy {
  private final int minAge;

  public AgePolicy(int minAge) {
    this.minAge = minAge;
  }

  @Override
  public boolean allows(Student student, Course course) {
    return student.age() >= minAge;
  }
}
