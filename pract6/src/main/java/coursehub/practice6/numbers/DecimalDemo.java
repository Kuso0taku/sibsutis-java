package coursehub.practice6.numbers;

import java.math.BigDecimal;

// три способа получить 0.1 — результат разный
public class DecimalDemo {
  public static void main(String[] args) {
    // double-конструктор берёт точное двоичное значение, а не «0.1»
    BigDecimal fromDouble = new BigDecimal(0.1);
    // valueOf округляет через Double.toString
    BigDecimal valueOf = BigDecimal.valueOf(0.1);
    // строковый конструктор читает десятичную запись как есть
    BigDecimal fromString = new BigDecimal("0.1");

    System.out.println("new BigDecimal(0.1): " + fromDouble);
    System.out.println("BigDecimal.valueOf(0.1): " + valueOf);
    System.out.println("new BigDecimal(\"0.1\"): " + fromString);
  }
}
