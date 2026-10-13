package task14;

// фиксированная цена независимо от base
public class Fixed implements PaymentPolicy {
  private final int fixedPrice;

  public Fixed(int fixedPrice) {
    this.fixedPrice = fixedPrice;
  }

  @Override
  public int price(int basePrice) {
    return fixedPrice;
  }
}
