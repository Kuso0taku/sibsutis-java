package task7;

import task2.NotificationSender;

// декоратор: считает отправки, делегирует работу внутрь
public class MetricsDecorator implements NotificationSender {
  private final NotificationSender delegate;
  private int sendCount;

  public MetricsDecorator(NotificationSender delegate) {
    this.delegate = delegate;
  }

  @Override
  public void send(String recipient, String message) {
    sendCount++;
    System.out.println("metrics: send #" + sendCount);
    delegate.send(recipient, message);
  }

  public int sendCount() { return sendCount; }
}
