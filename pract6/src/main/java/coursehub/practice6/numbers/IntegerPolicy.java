package coursehub.practice6.numbers;

// контракт работы с Integer: явно решаем, что делать с null
public final class IntegerPolicy {
  private IntegerPolicy() {
  }

  // unboxing без проверки: null приводит к NullPointerException
  public static int plusOneUnsafe(Integer value) {
    return value + 1;
  }
}
