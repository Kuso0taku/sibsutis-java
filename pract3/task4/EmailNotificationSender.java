package task4;

// email-вариант: только шаг отправки
public class EmailNotificationSender extends AbstractNotificationSender {
  @Override
  protected void doSend(String recipient, String message) {
    System.out.println("email to " + recipient + ": " + message);
  }
}
