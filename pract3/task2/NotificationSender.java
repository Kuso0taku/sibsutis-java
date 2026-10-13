package task2;

// контракт отправки уведомления
public interface NotificationSender {
  void send(String recipient, String message);
}
