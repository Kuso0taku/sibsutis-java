package task2;

// console-реализация
public class ConsoleSender implements NotificationSender {
  @Override
  public void send(String recipient, String message) {
    System.out.println("console[" + recipient + "]: " + message);
  }
}
