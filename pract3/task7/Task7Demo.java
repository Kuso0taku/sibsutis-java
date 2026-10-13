package task7;

import task2.EmailSender;
import task2.NotificationSender;

// вместо LoggingEmailSender extends EmailSender — композиция:
// порядок декораторов меняет наблюдаемое поведение
public class Task7Demo {
  public static void main(String[] args) {
    // порядок 1: сначала метрики, потом логирование
    System.out.println("== metrics -> logging ==");
    NotificationSender order1 =
        new MetricsDecorator(new LoggingDecorator(new EmailSender()));
    order1.send("a@example.com", "письмо 1");
    order1.send("a@example.com", "письмо 2");

    // порядок 2: сначала логирование, потом метрики
    System.out.println("== logging -> metrics ==");
    NotificationSender order2 =
        new LoggingDecorator(new MetricsDecorator(new EmailSender()));
    order2.send("b@example.com", "письмо 1");
  }
}
