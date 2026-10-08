package task15;

import src.Course;
import src.Student;
import task12.Accepted;
import task12.EnrollmentResult;

// композиция правил: все должны пройти, возвращается первое отклонение
public class AllOf implements Rule {
  private final Rule[] rules;

  public AllOf(Rule... rules) {
    this.rules = rules;
  }

  @Override
  public EnrollmentResult check(Student student, Course course) {
    EnrollmentResult lastAccepted = new Accepted(student.id(), course.id());
    for (Rule rule : rules) {
      EnrollmentResult result = rule.check(student, course);
      // проверяем тип результата, а не класс правила
      if (result instanceof Accepted accepted) {
        lastAccepted = accepted;
      } else {
        return result;
      }
    }
    return lastAccepted;
  }
}
