package task6;

// исправленный вариант: сигнтуры совпадают, доступ не сужается,
// возвращаемый тип covariant, везде @Override
class FixedBase {
  void send(String to) { System.out.println("base.send"); }
  protected void log(String msg) { System.out.println("base.log"); }
  Number value() { return 1; }
}

class Fixed extends FixedBase {
  @Override
  void send(String to) { System.out.println("fixed.send to " + to); }

  @Override
  protected void log(String msg) { System.out.println("fixed.log: " + msg); }

  @Override
  Integer value() { return 2; } // covariant: Integer — подтип Number
}

public class FixedOverride {
  public static void main(String[] args) {
    FixedBase obj = new Fixed();
    obj.send("a");
    obj.log("b");
    System.out.println(obj.value());
  }
}
