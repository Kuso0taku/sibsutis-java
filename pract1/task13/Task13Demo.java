package task13;

import src.Course;

// новый канал = новый класс, существующий код не трогаем
public class Task13Demo {
  public static void main(String[] args) {
    Course course = new Course(1, "Java", 10);

    new Notifier(new EmailChannel()).announce(course);
    new Notifier(new SmsChannel()).announce(course);
    new Notifier(new PushChannel()).announce(course);
  }
}
