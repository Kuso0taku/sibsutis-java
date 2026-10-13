package task13;

import src.Course;

// до рефакторинга: type code + switch на каждое новое значение
public class TypeCodeBefore {
  // каждое новое значение "notificationType" требует правки этого метода
  static String announce(String notificationType, Course course) {
    switch (notificationType) {
      case "email":
        return "email: курс " + course.title();
      case "sms":
        return "sms: курс " + course.title();
      case "push":
        return "push: курс " + course.title();
      default:
        throw new IllegalArgumentException("unknown type: " + notificationType);
    }
  }

  public static void main(String[] args) {
    Course course = new Course(1, "Java", 10);
    System.out.println(announce("email", course));
    System.out.println(announce("sms", course));
  }
}
