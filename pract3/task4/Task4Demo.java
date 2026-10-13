package task4;

// send() — final: проверка адресата общая, менять её нельзя;
// doSend() — abstract: единственное место для подклассов
public class Task4Demo {
  public static void main(String[] args) {
    AbstractNotificationSender email = new EmailNotificationSender();
    AbstractNotificationSender console = new ConsoleNotificationSender();

    // общий алгоритм: проверка + делегирование doSend конкретного класса
    email.send("student@example.com", "Java стартует");
    console.send("student@example.com", "Java стартует");

    try {
      email.send("  ", "никогда не отправится");
    } catch (IllegalArgumentException e) {
      System.out.println("проверка сработала: " + e.getMessage());
    }
  }
}
