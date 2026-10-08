package task15;

import src.Course;
import src.Student;
import task12.Accepted;
import task12.EnrollmentResult;
import task12.Rejected;

// правило: курс открыт
public class CourseOpenRule implements Rule {
  @Override
  public EnrollmentResult check(Student student, Course course) {
    if (course.status() != Course.Status.OPEN) {
      return new Rejected(student.id(), course.id(), "course closed");
    }
    return new Accepted(student.id(), course.id());
  }
}
