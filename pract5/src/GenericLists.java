package src;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;

// Утилиты над списками. Методы generic и не привязаны к предметной области.
public final class GenericLists {
  private GenericLists() {
  }

  // firstOrThrow: явная ошибка вместо null для пустого списка
  public static <T> T firstOrThrow(List<T> list) {
    if (list == null) throw new NullPointerException("list must not be null");
    if (list.isEmpty()) throw new java.util.NoSuchElementException("list is empty");
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

  // indexBy: строит Map<K, T> по ключу из элемента.
  // Конфликт ключей не затирается молча - это ошибка вызывающего кода.
  public static <T, K> Map<K, T> indexBy(List<T> items, Function<T, K> keyFn) {
    Map<K, T> index = new LinkedHashMap<>();
    for (T item : items) {
      if (item == null) throw new NullPointerException("item must not be null");
      K key = keyFn.apply(item);
      if (key == null) throw new NullPointerException("key must not be null");
      T previous = index.putIfAbsent(key, item);
      if (previous != null) {
        throw new IllegalStateException(
            "duplicate key " + key + ": " + previous + " and " + item);
      }
    }
    return index;
  }

  // Версия с явной стратегией слияния для случая, когда конфликт ожидаем.
  public static <T, K> Map<K, T> indexBy(
      List<T> items, Function<T, K> keyFn, BinaryOperator<T> merge) {
    Map<K, T> index = new LinkedHashMap<>();
    for (T item : items) {
      if (item == null) throw new NullPointerException("item must not be null");
      index.merge(keyFn.apply(item), item, merge);
    }
    return index;
  }
}
