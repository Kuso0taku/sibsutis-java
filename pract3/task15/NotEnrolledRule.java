package task15;

import src.Course;
import src.Student;
import task12.Accepted;
import task12.EnrollmentResult;
import task12.Rejected;

// правило: студент ещё не записан на этот курс
public class NotEnrolledRule implements Rule {
  @Override
  public EnrollmentResult check(Student student, Course course) {
    if (course.enrolledStudents().contains(student.id())) {
      return new Rejected(student.id(), course.id(), "already enrolled");
    }
    return new Accepted(student.id(), course.id());
  }
}
