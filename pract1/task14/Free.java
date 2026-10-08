package task14;

public class Free implements PaymentPolicy {
  @Override
  public int price(int basePrice) {
    return 0;
  }
}
