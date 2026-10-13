package task4;

// console-вариант: только шаг отправки
public class ConsoleNotificationSender extends AbstractNotificationSender {
  @Override
  protected void doSend(String recipient, String message) {
    System.out.println("console[" + recipient + "]: " + message);
  }
}
