package src;

import java.util.LinkedHashMap;
import java.util.Map;

// Кэш ограниченной емкости по принципу LRU.
// Реализован на стандартных коллекциях: LinkedHashMap с accessOrder=true
// переставляет прочитанный ключ в конец, а removeEldestEntry вытесняет
// самый старый (первый по порядку) при превышении емкости.
public class LruCache<K, V> {
  private final int capacity;
  private final Map<K, V> map;

  public LruCache(int capacity) {
    if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
    this.capacity = capacity;
    this.map = new LinkedHashMap<>(capacity, 0.75f, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > LruCache.this.capacity;
      }
    };
  }

  // get обновляет недавность: после чтения ключ становится самым свежим
  public V get(K key) {
    if (key == null) throw new NullPointerException("key must not be null");
    return map.get(key);
  }

  public void put(K key, V value) {
    if (key == null) throw new NullPointerException("key must not be null");
    if (value == null) throw new NullPointerException("value must not be null");
    map.put(key, value);
  }

  public boolean containsKey(K key) {
    if (key == null) throw new NullPointerException("key must not be null");
    return map.containsKey(key);
  }

  public int size() {
    return map.size();
  }

  public void clear() {
    map.clear();
  }
}