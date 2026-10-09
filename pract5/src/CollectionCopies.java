package src;

import java.util.List;

// PECS: Producer Extends, Consumer Super.
// Источник читаем - значит producer и ? extends T.
// Приемник только пишем - значит consumer и ? super T.
public final class CollectionCopies {
  private CollectionCopies() {
  }

  public static <T> void copy(List<? extends T> source, List<? super T> target) {
    if (source == null || target == null) {
      throw new NullPointerException("lists must not be null");
    }
    for (T item : source) {
      target.add(item);
    }
  }

  // Тот же принцип без промежуточного T: копируем как есть.
  public static void copyRaw(List<? extends Object> source, List<? super Object> target) {
    for (Object item : source) {
      target.add(item);
    }
  }
}
