package task15;

import src.Course;
import src.Student;
import task12.Accepted;
import task12.EnrollmentResult;
import task12.Rejected;

// правило: пререквизиты курса
public class PrerequisiteRule implements Rule {
  @Override
  public EnrollmentResult check(Student student, Course course) {
    if (course.hasPrerequisite() && !student.hasPrerequisite()) {
      return new Rejected(student.id(), course.id(), "prerequisite missing");
    }
    return new Accepted(student.id(), course.id());
  }
}
