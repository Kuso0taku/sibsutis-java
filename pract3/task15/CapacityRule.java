package task15;

import src.Course;
import src.Student;
import task12.Accepted;
import task12.EnrollmentResult;
import task12.Rejected;

// правило: есть свободные места
public class CapacityRule implements Rule {
  @Override
  public EnrollmentResult check(Student student, Course course) {
    if (course.seatsLeft() <= 0) {
      return new Rejected(student.id(), course.id(), "no seats left");
    }
    return new Accepted(student.id(), course.id());
  }
}
