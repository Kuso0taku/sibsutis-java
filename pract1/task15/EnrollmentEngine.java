package task15;

import src.Course;
import src.Student;
import task12.EnrollmentResult;

// сервис зависит только от контракта Rule: ни одного instanceof по классам правил
public class EnrollmentEngine {
  private final Rule rule;

  public EnrollmentEngine(Rule rule) {
    this.rule = rule;
  }

  public EnrollmentResult evaluate(Student student, Course course) {
    return rule.check(student, course);
  }
}
