package task10;

// Намеренно не компилируется (исключено из pom.xml).
// Два default-метода с одной сигнатурой — наследование поведения неоднозначно.
// Запуск вручную: javac pract1/task10/BrokenDefaults.java

interface BrokenNotifier {
  default String name() { return "notifier"; }
}

interface BrokenLogger {
  default String name() { return "logger"; }
}

class BrokenNotifierLogger implements BrokenNotifier, BrokenLogger {
  // ошибка компиляции: name() неоднозначен, нужен явный override
}
