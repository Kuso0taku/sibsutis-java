package task14;

// подписка: базовая цена делится на 12 месяцев
public class Subscription implements PaymentPolicy {
  @Override
  public int price(int basePrice) {
    return basePrice / 12;
  }
}
