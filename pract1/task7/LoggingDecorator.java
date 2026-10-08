package task7;

import task2.NotificationSender;

// декоратор: логирует факт отправки, делегирует работу внутрь
public class LoggingDecorator implements NotificationSender {
  private final NotificationSender delegate;

  public LoggingDecorator(NotificationSender delegate) {
    this.delegate = delegate;
  }

  @Override
  public void send(String recipient, String message) {
    System.out.println("log: start send to " + recipient);
    delegate.send(recipient, message);
    System.out.println("log: end send to " + recipient);
  }
}
