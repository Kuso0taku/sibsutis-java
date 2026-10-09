import org.junit.jupiter.api.Test;
import src.LruCache;

import static org.junit.jupiter.api.Assertions.*;

class LruCacheTest {
  @Test
  void putBeyondCapacity_evictsOldest() {
    // Arrange
    LruCache<String, Integer> cache = new LruCache<>(3);

    // Act
    cache.put("a", 1);
    cache.put("b", 2);
    cache.put("c", 3);
    cache.put("d", 4); // должен вытеснить "a"

    // Assert
    assertEquals(3, cache.size());
    assertFalse(cache.containsKey("a"));
    assertTrue(cache.containsKey("b"));
    assertTrue(cache.containsKey("d"));
  }

  @Test
  void get_refreshesRecencyAndPreventsEviction() {
    // Arrange
    LruCache<String, Integer> cache = new LruCache<>(3);
    cache.put("a", 1);
    cache.put("b", 2);
    cache.put("c", 3);

    // Act: читаем "a" - теперь самый свежий, самый старый - "b"
    cache.get("a");
    cache.put("d", 4);

    // Assert: вытеснен "b", а не "a"
    assertFalse(cache.containsKey("b"));
    assertTrue(cache.containsKey("a"));
    assertTrue(cache.containsKey("c"));
    assertTrue(cache.containsKey("d"));
  }

  @Test
  void repeatedPutOnExistingKey_updatesValueWithoutSizeGrowth() {
    // Arrange
    LruCache<String, Integer> cache = new LruCache<>(2);
    cache.put("a", 1);

    // Act
    cache.put("a", 99);
    cache.put("b", 2);

    // Assert
    assertEquals(2, cache.size());
    assertEquals(99, cache.get("a"));
  }

  @Test
  void getOfAbsentKey_returnsNullAndDoesNotEvict() {
    // Arrange
    LruCache<String, Integer> cache = new LruCache<>(2);
    cache.put("a", 1);
    cache.put("b", 2);

    // Act
    Integer miss = cache.get("missing");

    // Assert
    assertNull(miss, "промах = null, потому что null-значения не храним");
    assertEquals(2, cache.size());
  }

  @Test
  void clear_resetsCache() {
    // Arrange
    LruCache<String, Integer> cache = new LruCache<>(2);
    cache.put("a", 1);

    // Act
    cache.clear();

    // Assert
    assertEquals(0, cache.size());
    assertFalse(cache.containsKey("a"));
  }

  @Test
  void nullKeysAndValues_areRejected() {
    // Arrange
    LruCache<String, Integer> cache = new LruCache<>(2);

    // Act + Assert: null-политика явная - ни ключ, ни значение не null
    assertThrows(NullPointerException.class, () -> cache.put(null, 1));
    assertThrows(NullPointerException.class, () -> cache.put("a", null));
    assertThrows(NullPointerException.class, () -> cache.get(null));
  }

  @Test
  void invalidCapacity_isRejected() {
    // Act + Assert
    assertThrows(IllegalArgumentException.class, () -> new LruCache<Integer, Integer>(0));
    assertThrows(IllegalArgumentException.class, () -> new LruCache<Integer, Integer>(-1));
  }

  @Test
  void keysThatAreEqualButNotSame_areFound() {
    // Arrange: ключи равны по equals, но это разные объекты
    LruCache<String, Integer> cache = new LruCache<>(1);
    cache.put(new String("a"), 1);

    // Act + Assert: стабильность ключей - поиск по равенству, не по ссылке
    assertEquals(1, cache.get(new String("a")));
  }
}