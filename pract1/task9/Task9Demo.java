package task9;

import src.Course;
import src.Student;

// новая политика подключается без изменения EnrollmentService
public class Task9Demo {
  public static void main(String[] args) {
    Course java = new Course(1, "Java", 10);
    java.setHasPrerequisite(true);

    Student adult = new Student(1, "Аня", 20, true);
    Student kid = new Student(2, "Боря", 12, true);
    Student noExp = new Student(3, "Вера", 25, false);

    // комбинация политик через лямбды: сервис тот же
    EnrollmentPolicy combined = (student, course) ->
        new AgePolicy(16).allows(student, course)
            && new PrerequisitePolicy().allows(student, course)
            && new CapacityPolicy().allows(student, course);

    EnrollmentService service = new EnrollmentService(combined);

    System.out.println(service.enroll(adult, java));  // true
    System.out.println(service.enroll(kid, java));    // false: возраст
    System.out.println(service.enroll(noExp, java));  // false: пререквизит

    // ещё одна новая политика — снова без правок сервиса
    EnrollmentPolicy freeOnly = (student, course) -> course.id() % 2 == 0;
    System.out.println(new EnrollmentService(freeOnly).enroll(adult, java)); // false
  }
}
