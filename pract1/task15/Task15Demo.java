package task15;

import src.Course;
import src.Student;
import task12.Accepted;
import task12.Deferred;
import task12.EnrollmentResult;
import task12.Rejected;
import task12.WaitListed;

// dynamic dispatch: rule.check(...) выполняется на классе каждого правила,
// engine знать эти классы не нужно
public class Task15Demo {
  static void print(EnrollmentResult result) {
    System.out.println(switch (result) {
      case Accepted a -> "accepted: student " + a.studentId();
      case Rejected r -> "rejected: " + r.reason();
      case WaitListed w -> "waitlisted #" + w.position();
      case Deferred d -> "deferred to " + d.semester();
    });
  }

  public static void main(String[] args) {
    Course java = new Course(1, "Java", 10);
    java.setHasPrerequisite(true);
    java.setCapacity(2);

    // пять открытых стратегий, композиция allOf
    Rule defaultPolicy = new AllOf(
        new CourseOpenRule(),
        new CapacityRule(),
        new NotEnrolledRule(),
        new PrerequisiteRule(),
        new AgeRule(16));

    EnrollmentEngine engine = new EnrollmentEngine(defaultPolicy);

    Student anya = new Student(1, "Аня", 20, true);
    Student borya = new Student(2, "Боря", 12, true);
    Student vera = new Student(3, "Вера", 25, false);

    print(engine.evaluate(anya, java)); // accepted
    java.addEnrolled(anya.id());
    print(engine.evaluate(anya, java)); // rejected: already enrolled
    print(engine.evaluate(borya, java)); // rejected: too young
    print(engine.evaluate(vera, java)); // rejected: prerequisite missing

    java.setStatus(Course.Status.CLOSED);
    print(engine.evaluate(borya, java)); // rejected: course closed (другое правило)

    // новая политика — новая стратегия, engine не меняется
    java.setStatus(Course.Status.OPEN);
    Rule anyOfAgeOrOpen = new AnyOf(new AgeRule(16), new CourseOpenRule());
    System.out.println("anyOf:");
    print(new EnrollmentEngine(anyOfAgeOrOpen).evaluate(borya, java));
  }
}
