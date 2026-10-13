package task13;

import src.Course;

public class EmailChannel implements NotificationChannel {
  @Override
  public void announce(Course course) {
    System.out.println("email: курс " + course.title());
  }
}
