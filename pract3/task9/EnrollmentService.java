package task9;

import src.Course;
import src.Student;

// сервис зависит только от абстракции EnrollmentPolicy
public class EnrollmentService {
  private final EnrollmentPolicy policy;

  public EnrollmentService(EnrollmentPolicy policy) {
    this.policy = policy;
  }

  public boolean enroll(Student student, Course course) {
    if (!policy.allows(student, course)) {
      return false;
    }
    course.addEnrolled(student.id());
    return true;
  }
}
