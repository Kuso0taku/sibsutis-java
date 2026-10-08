package task13;

import src.Course;

// клиент не знает о конкретных каналах и о type code
public class Notifier {
  private final NotificationChannel channel;

  public Notifier(NotificationChannel channel) {
    this.channel = channel;
  }

  public void announce(Course course) {
    channel.announce(course);
  }
}
