package task12;

// exhaustive switch без default: новый permitted-тип ломает компиляцию до добавления ветки
public class Task12Demo {
  static String describe(EnrollmentResult result) {
    return switch (result) {
      case Accepted a -> "accepted: student " + a.studentId();
      case Rejected r -> "rejected: " + r.reason();
      case WaitListed w -> "waitlisted #" + w.position();
      case Deferred d -> "deferred to " + d.semester();
    };
  }

  public static void main(String[] args) {
    System.out.println(describe(new Accepted(1, 10)));
    System.out.println(describe(new Rejected(2, 10, "course full")));
    System.out.println(describe(new WaitListed(3, 10, 5)));
    System.out.println(describe(new Deferred(4, 10, "spring")));
  }
}
