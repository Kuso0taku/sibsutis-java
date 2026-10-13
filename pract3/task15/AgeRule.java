package task15;

import src.Course;
import src.Student;
import task12.Accepted;
import task12.EnrollmentResult;
import task12.Rejected;

// правило: минимальный возраст
public class AgeRule implements Rule {
  private final int minAge;

  public AgeRule(int minAge) {
    this.minAge = minAge;
  }

  @Override
  public EnrollmentResult check(Student student, Course course) {
    if (student.age() < minAge) {
      return new Rejected(student.id(), course.id(), "too young, min age " + minAge);
    }
    return new Accepted(student.id(), course.id());
  }
}
