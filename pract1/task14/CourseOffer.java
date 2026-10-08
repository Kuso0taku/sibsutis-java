package task14;

// вместо девяти подклассов (Online+Free, Online+Fixed, ...) — композиция осей
public class CourseOffer {
  private final CourseFormat format;
  private final PaymentPolicy payment;

  public CourseOffer(CourseFormat format, PaymentPolicy payment) {
    this.format = format;
    this.payment = payment;
  }

  public String describe(int basePrice) {
    return format.describe() + " / " + payment.price(basePrice) + " RUB";
  }
}
