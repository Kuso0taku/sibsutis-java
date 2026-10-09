package src;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

// Result<T> - либо значение, либо ошибка с текстом. Без raw types:
// параметр T фиксируется при создании, компилятор не даст смешать типы.
// Инвариант: ровно одно из двух состояний заполнено.
public final class Result<T> {
  private final T value;      // != null при успехе
  private final String error; // != null при ошибке

  private Result(T value, String error) {
    if ((value == null) == (error == null)) {
      throw new IllegalArgumentException("exactly one of value/error must be set");
    }
    this.value = value;
    this.error = error;
  }

  public static <T> Result<T> ok(T value) {
    if (value == null) throw new NullPointerException("value must not be null");
    return new Result<>(value, null);
  }

  public static <T> Result<T> error(String message) {
    if (message == null || message.isBlank()) {
      throw new IllegalArgumentException("message must not be blank");
    }
    return new Result<>(null, message);
  }

  public boolean isOk() {
    return error == null;
  }

  public boolean isError() {
    return error != null;
  }

  public T value() {
    if (isError()) throw new IllegalStateException("no value: " + error);
    return value;
  }

  public String error() {
    if (isOk()) throw new IllegalStateException("result is ok, no error");
    return error;
  }

  public Optional<T> toOptional() {
    return Optional.ofNullable(value);
  }

  // map/ flatMap меняют только успешное значение, ошибка проходит насквозь
  public <R> Result<R> map(Function<? super T, ? extends R> mapper) {
    if (isError()) return error(error);
    return ok(mapper.apply(value));
  }

  public <R> Result<R> flatMap(Function<? super T, Result<R>> mapper) {
    if (isError()) return error(error);
    return mapper.apply(value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Result<?> other)) return false;
    return Objects.equals(value, other.value) && Objects.equals(error, other.error);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, error);
  }

  @Override
  public String toString() {
    return isOk() ? "Ok(" + value + ")" : "Error(" + error + ")";
  }
}
