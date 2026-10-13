package task2;

// разделение overload resolution (compile-time типы) и override dispatch (runtime класс)
public class Task2Demo {

  // перегруженные методы: какая перегрузка вызовется — решается по compile-time типу
  static String describe(NotificationSender sender) { return "by interface"; }
  static String describe(EmailSender sender) { return "by concrete"; }

  // override dispatch: внутри метода вызовется метод фактического класса объекта
  static void report(NotificationSender sender) {
    sender.send("student@example.com", "курс открыт");
    // перегрузка выбирается по типу параметра, а не по классу объекта
    System.out.println("  overload -> " + describe(sender));
  }

  public static void main(String[] args) {
    NotificationSender asInterface = new EmailSender();
    EmailSender asConcrete = new EmailSender();
    NotificationSender console = new ConsoleSender();

    report(asInterface); // send -> EmailSender, describe -> "by interface"
    report(console);     // send -> ConsoleSender, describe -> "by interface"
    report(asConcrete);  // send -> EmailSender, describe -> "by interface"

    System.out.println(describe(asInterface)); // by interface
    System.out.println(describe(asConcrete));  // by concrete
  }
}
