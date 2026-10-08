package task13;

import src.Course;

public class SmsChannel implements NotificationChannel {
  @Override
  public void announce(Course course) {
    System.out.println("sms: курс " + course.title());
  }
}
