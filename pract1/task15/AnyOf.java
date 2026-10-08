package task15;

import src.Course;
import src.Student;
import task12.Accepted;
import task12.EnrollmentResult;
import task12.Rejected;

// композиция правил: достаточно пройти любое, иначе первое отклонение
public class AnyOf implements Rule {
  private final Rule[] rules;

  public AnyOf(Rule... rules) {
    this.rules = rules;
  }

  @Override
  public EnrollmentResult check(Student student, Course course) {
    EnrollmentResult firstRejected = null;
    for (Rule rule : rules) {
      EnrollmentResult result = rule.check(student, course);
      if (result instanceof Accepted) {
        return result;
      }
      if (firstRejected == null) {
        firstRejected = result;
      }
    }
    return firstRejected != null
        ? firstRejected
        : new Rejected(student.id(), course.id(), "no rules configured");
  }
}
