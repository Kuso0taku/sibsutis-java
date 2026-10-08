package task6;

import task2.EmailSender;
import task2.NotificationSender;

// компилируется без @Override, но вызывается не тот метод:
// в Sub это перегрузка, а не переопределение
class HandleBase {
  void handle(NotificationSender sender) { System.out.println("base.handle(NotificationSender)"); }
}

class HandleSub extends HandleBase {
  // хотелось переопределить, а получилась перегрузка — @Override показал бы ошибку
  void handle(EmailSender sender) { System.out.println("sub.handle(EmailSender)"); }
}

public class Task6Demo {
  public static void main(String[] args) {
    // compile-time тип HandleBase -> вызовется base.handle, хотя класс объекта HandleSub
    HandleBase byBaseType = new HandleSub();
    byBaseType.handle(new EmailSender());

    // compile-time тип HandleSub -> перегрузка по EmailSender
    HandleSub bySubType = new HandleSub();
    bySubType.handle(new EmailSender());
  }
}
