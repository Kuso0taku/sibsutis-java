package task14;

// ось оплаты: независима от формата
public interface PaymentPolicy {
  int price(int basePrice);
}
