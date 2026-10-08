package task10;

// после явного override компилятор молча берёт реализацию класса
public class Task10Demo {
  public static void main(String[] args) {
    NotifierLogger both = new NotifierLogger();
    System.out.println(both.name()); // notifier+logger

    // default-методы интерфейсов доступны по отдельности
    Notifier asNotifier = both;
    Logger asLogger = both;
    // но name() уже переопределён в классе — печатает одинаково
    System.out.println(asNotifier.name());
    System.out.println(asLogger.name());
  }
}
