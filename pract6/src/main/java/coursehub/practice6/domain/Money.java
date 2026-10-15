package coursehub.practice6.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

// неизменяемый денежный тип: сумма и валюта, без double
public final class Money {
  private final BigDecimal amount;
  private final Currency currency;

  private Money(BigDecimal amount, Currency currency) {
    if (amount == null || currency == null) {
      throw new IllegalArgumentException("amount and currency must not be null");
    }
    // явное округление до копеек валюты
    this.amount = amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_UP);
    this.currency = currency;
  }

  public static Money of(BigDecimal amount, Currency currency) {
    return new Money(amount, currency);
  }

  public static Money of(String amount, Currency currency) {
    return new Money(new BigDecimal(amount), currency);
  }

  public BigDecimal amount() {
    return amount;
  }

  public Currency currency() {
    return currency;
  }

  public Money add(Money other) {
    requireSameCurrency(other);
    return new Money(amount.add(other.amount), currency);
  }

  public Money subtract(Money other) {
    requireSameCurrency(other);
    return new Money(amount.subtract(other.amount), currency);
  }

  // смешивать валюты нельзя: это ошибка модели, а не повод молча сложить числа
  private void requireSameCurrency(Money other) {
    if (!currency.equals(other.currency)) {
      throw new IllegalArgumentException("currency mismatch: " + currency + " vs " + other.currency);
    }
  }
}
