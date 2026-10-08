package task13;

import src.Course;

// различия поведения уехали из switch в реализации интерфейса
public interface NotificationChannel {
  void announce(Course course);
}
