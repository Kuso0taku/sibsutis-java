package task4;

import task2.NotificationSender;

// абстрактный базовый класс: общая проверка адресата + точка расширения doSend
public abstract class AbstractNotificationSender implements NotificationSender {

  // публичный алгоритм фиксирован: сначала проверка, потом шаг реализации
  @Override
  public final void send(String recipient, String message) {
    if (recipient == null || recipient.isBlank()) {
      throw new IllegalArgumentException("recipient must not be blank");
    }
    doSend(recipient, message);
  }

  // точка расширения: подклассы решают, как именно отправлять
  protected abstract void doSend(String recipient, String message);
}
