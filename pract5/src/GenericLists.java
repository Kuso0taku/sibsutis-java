package src;

import java.util.List;
import java.util.NoSuchElementException;

// Утилиты над списками. Методы generic и не привязаны к предметной области.
public final class GenericLists {
  private GenericLists() {
  }

  // firstOrThrow: явная ошибка вместо null для пустого списка
  public static <T> T firstOrThrow(List<T> list) {
    if (list == null) throw new NullPointerException("list must not be null");
    if (list.isEmpty()) throw new NoSuchElementException("list is empty");
    return list.get(0);
  }

  // swap: меняет местами два элемента, границы проверяются заранее
  public static <T> void swap(List<T> list, int i, int j) {
    if (list == null) throw new NullPointerException("list must not be null");
    if (i < 0 || j < 0 || i >= list.size() || j >= list.size()) {
      throw new IndexOutOfBoundsException("index out of range: " + i + ", " + j);
    }
    T tmp = list.get(i);
    list.set(i, list.get(j));
    list.set(j, tmp);
  }
}
