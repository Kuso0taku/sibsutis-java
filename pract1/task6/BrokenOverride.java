package task6;

// Намеренно не компилируется (исключено из pom.xml).
// Ошибки: чужой параметр при @Override, сужение доступа, несвязанный возвращаемый тип.
// Запуск вручную: javac pract1/task6/BrokenOverride.java

class Base {
  void send(String to) { System.out.println("base.send"); }
  void log(String msg) { System.out.println("base.log"); }
  Number value() { return 1; }
}

class Broken extends Base {
  // ошибка: другой параметр — это новая перегрузка, @Override нечего переопределять
  @Override
  void send(int to) { System.out.println("broken.send"); }

  // ошибка: нельзя сужать доступ при переопределении (package-private -> private)
  @Override
  private void log(String msg) { System.out.println("broken.log"); }

  // ошибка: String не подтип Number — неcovariant return
  @Override
  String value() { return "1"; }
}
