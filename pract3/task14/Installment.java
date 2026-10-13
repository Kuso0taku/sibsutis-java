package task14;

// новая оплата: только новый класс, остальной код не тронут
public class Installment implements PaymentPolicy {
  private final int parts;

  public Installment(int parts) {
    if (parts <= 0) throw new IllegalArgumentException("parts must be > 0");
    this.parts = parts;
  }

  @Override
  public int price(int basePrice) {
    return basePrice / parts;
  }
}
