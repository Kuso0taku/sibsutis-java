package task2;

// email-реализация
public class EmailSender implements NotificationSender {
  @Override
  public void send(String recipient, String message) {
    System.out.println("email to " + recipient + ": " + message);
  }
}
