package src;

import java.util.List;
import java.util.NoSuchElementException;

// max/min для элементов, которые умеют сравниваться.
//
// Граница: T extends Comparable<? super T>, а не T extends Comparable<T>.
// "? super T" разрешает использовать тип, у которого compareTo объявлен
// в супертипе (важно для подтипов и для типов вроде java.sql.Timestamp,
// где compareTo наследуется от java.util.Date).
public final class Comparables {
  private Comparables() {
  }

  public static <T extends Comparable<? super T>> T max(List<? extends T> items) {
    if (items == null) throw new NullPointerException("items must not be null");
    if (items.isEmpty()) throw new NoSuchElementException("items is empty");
    T best = items.get(0);
    for (T item : items) {
      if (item.compareTo(best) > 0) {
        best = item;
      }
    }
    return best;
  }

  public static <T extends Comparable<? super T>> T min(List<? extends T> items) {
    if (items == null) throw new NullPointerException("items must not be null");
    if (items.isEmpty()) throw new NoSuchElementException("items is empty");
    T best = items.get(0);
    for (T item : items) {
      if (item.compareTo(best) < 0) {
        best = item;
      }
    }
    return best;
  }
}
