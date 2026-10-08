package task13;

import src.Course;

public class PushChannel implements NotificationChannel {
  @Override
  public void announce(Course course) {
    System.out.println("push: курс " + course.title());
  }
}
