package task10;

// те же два интерфейса, конфликт разрешён явным override в классе
interface Notifier {
  default String name() { return "notifier"; }
}

interface Logger {
  default String name() { return "logger"; }
}

// множественное наследование поведения требует явного выбора класса
class NotifierLogger implements Notifier, Logger {
  @Override
  public String name() { return "notifier+logger"; }
}
