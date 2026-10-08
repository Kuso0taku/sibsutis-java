package task9;

import src.Course;
import src.Student;

// политика по пререквизитам: если курс их требует — у студента должен быть опыт
public class PrerequisitePolicy implements EnrollmentPolicy {
  @Override
  public boolean allows(Student student, Course course) {
    return !course.hasPrerequisite() || student.hasPrerequisite();
  }
}
