package task15;

import src.Course;
import src.Student;
import task12.EnrollmentResult;

// открытая стратегия правила: типизированный результат — sealed EnrollmentResult из task12
public interface Rule {
  EnrollmentResult check(Student student, Course course);
}
