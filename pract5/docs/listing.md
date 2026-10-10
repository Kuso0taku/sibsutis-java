# Листинги pract5

Полные исходники всех заданий. В заголовке каждого листинга указано имя файла,
а в скобках — номер(а) задания. Если файл используется в нескольких заданиях,
перечислены все эти задания.

> Пояснения «на словах» — в `taskN/answer.md`, полный разбор — в `practice5.md`.

## CourseRegistry.java (задание 1)

```java
import src.CourseCode;

import java.util.ArrayList;
import java.util.List;

// Список регистраций - это именно List: важен порядок появления.
// ArrayList выбран за O(1) доступ по индексу и хорошую локальность
// при обходе (см. ListPerformanceDemo).
public class CourseRegistry {
  private final List<CourseCode> registrations = new ArrayList<>();

  public void register(CourseCode code) {
    if (code == null) throw new NullPointerException("code must not be null");
    registrations.add(code);
  }

  public void insertAt(int index, CourseCode code) {
    // вставка в середину сохраняет порядок, но в ArrayList это сдвиг хвоста
    registrations.add(index, code);
  }

  public boolean remove(CourseCode code) {
    return registrations.remove(code);
  }

  public CourseCode at(int index) {
    return registrations.get(index);
  }

  public List<CourseCode> sequence() {
    // наружу отдаем копию, а не внутренний изменяемый список
    return List.copyOf(registrations);
  }

  public int size() {
    return registrations.size();
  }
}
```

## CourseRegistryTest.java (задание 1)

```java
import org.junit.jupiter.api.Test;
import src.CourseCode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// ArrayList как List: проверяем главное - порядок и позиции.
// Имя теста описывает наблюдаемое поведение, а не реализацию.
class CourseRegistryTest {
  private static CourseCode c(String raw) {
    return CourseCode.parse(raw);
  }

  @Test
  void register_keepsInsertionOrder() {
    // Arrange
    CourseRegistry registry = new CourseRegistry();

    // Act
    registry.register(c("java-101"));
    registry.register(c("KOTLIN-201"));
    registry.register(c("sql-301"));

    // Assert: List не переупорядочивает элементы
    assertEquals(List.of(c("JAVA101"), c("KOTLIN201"), c("SQL301")),
        registry.sequence());
  }

  @Test
  void insertAt_putsElementInTheMiddleAndShiftsTail() {
    // Arrange
    CourseRegistry registry = new CourseRegistry();
    registry.register(c("java-101"));
    registry.register(c("sql-301"));

    // Act
    registry.insertAt(1, c("kotlin-201"));

    // Assert
    assertEquals(c("KOTLIN201"), registry.at(1));
    assertEquals(List.of(c("JAVA101"), c("KOTLIN201"), c("SQL301")),
        registry.sequence());
  }

  @Test
  void remove_removesFirstOccurrenceOnly() {
    // Arrange: один и тот же код зарегистрирован дважды
    CourseRegistry registry = new CourseRegistry();
    registry.register(c("java-101"));
    registry.register(c("sql-301"));
    registry.register(c("JAVA101"));

    // Act
    boolean removed = registry.remove(c("java-101"));

    // Assert: удалилась первая позиция, вторая осталась
    assertTrue(removed);
    assertEquals(List.of(c("SQL301"), c("JAVA101")), registry.sequence());
  }

  @Test
  void sequence_isAnImmutableCopy() {
    // Arrange
    CourseRegistry registry = new CourseRegistry();
    registry.register(c("java-101"));

    // Act
    List<CourseCode> snapshot = registry.sequence();

    // Assert: наружу не утекает внутренний изменяемый список
    assertThrows(UnsupportedOperationException.class,
        () -> snapshot.add(c("sql-301")));
    assertEquals(1, registry.size());
  }

  @Test
  void duplicates_reportsInOrderOfFirstRepeat() {
    // Arrange
    List<CourseCode> codes = List.of(
        c("java-101"), c("sql-301"), c("JAVA101"), c("kotlin-201"), c("SQL301"));

    // Act
    List<CourseCode> duplicates = DuplicateFinder.duplicates(codes);

    // Assert: "java-101" повторился раньше "sql-301"
    assertEquals(List.of(c("JAVA101"), c("SQL301")), duplicates);
  }
}
```

## DuplicateFinder.java (задание 1)

```java
import src.CourseCode;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Поиск дубликатов одним проходом: seen хранит уже встречавшиеся,
// duplicates - LinkedHashSet, чтобы вернуть их в порядке первого повтора.
public class DuplicateFinder {
  public static List<CourseCode> duplicates(List<CourseCode> codes) {
    Set<CourseCode> seen = new HashSet<>();
    Set<CourseCode> duplicates = new LinkedHashSet<>();
    for (CourseCode code : codes) {
      if (!seen.add(code)) {
        duplicates.add(code);
      }
    }
    return List.copyOf(duplicates);
  }
}
```

## ListPerformanceDemo.java (задание 1)

```java
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

// Простой замер, А НЕ микробенчмарк: нет прогрева, JIT и GC не контролируются.
// Цель - показать порядок величин, а не точные числа.
// Чтение по индексу: ArrayList O(1), LinkedList O(n) - разница огромна.
// Обход: обе O(n), но ArrayList быстрее за счет непрерывной памяти.
public class ListPerformanceDemo {
  private static final int N = 100_000;
  private static final int READS = 200_000;

  public static void main(String[] args) {
    List<Integer> arrayList = fill(new ArrayList<>());
    List<Integer> linkedList = fill(new LinkedList<>());

    System.out.println("random access: arrayList=" + randomAccess(arrayList)
        + " ms, linkedList=" + randomAccess(linkedList) + " ms");
    System.out.println("traversal:     arrayList=" + traversal(arrayList)
        + " ms, linkedList=" + traversal(linkedList) + " ms");
  }

  static List<Integer> fill(List<Integer> list) {
    for (int i = 0; i < N; i++) list.add(i);
    return list;
  }

  static long randomAccess(List<Integer> list) {
    Random random = new Random(1);
    long sum = 0;
    long start = System.nanoTime();
    for (int i = 0; i < READS; i++) {
      sum += list.get(random.nextInt(N));
    }
    long elapsed = System.nanoTime() - start;
    if (sum == Long.MIN_VALUE) System.out.print(""); // не дать выкинуть цикл
    return elapsed / 1_000_000;
  }

  static long traversal(List<Integer> list) {
    long sum = 0;
    long start = System.nanoTime();
    for (int value : list) {
      sum += value;
    }
    long elapsed = System.nanoTime() - start;
    if (sum == Long.MIN_VALUE) System.out.print("");
    return elapsed / 1_000_000;
  }
}
```

## CourseCode.java (задания 1, 2, 5)

```java
package src;

import java.util.Locale;

// Код курса приходит из разных источников ("java-101", " JAVA_101 ", "Java 101"),
// но в системе он должен быть один. Нормализация убирает пробелы и разделители
// и приводит к верхнему регистру, и только потом проверяется.
public final class CourseCode {
  public static final int MIN_LENGTH = 3;
  public static final int MAX_LENGTH = 10;

  private final String value;

  private CourseCode(String value) {
    this.value = value;
  }

  public static CourseCode parse(String raw) {
    // null - ошибка программиста, а не плохие данные
    String normalized = normalize(raw);
    if (normalized.isEmpty()) {
      throw new IllegalArgumentException("code must not be blank");
    }
    if (normalized.length() < MIN_LENGTH || normalized.length() > MAX_LENGTH) {
      throw new IllegalArgumentException(
          "code length must be in [" + MIN_LENGTH + ", " + MAX_LENGTH + "]");
    }
    for (int i = 0; i < normalized.length(); i++) {
      char c = normalized.charAt(i);
      boolean latinLetterOrDigit = (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
      if (!latinLetterOrDigit) {
        throw new IllegalArgumentException(
            "code must contain only latin letters and digits, got '" + c + "'");
      }
    }
    return new CourseCode(normalized);
  }

  public static String normalize(String raw) {
    return raw.trim()
        .toUpperCase(Locale.ROOT)
        .replace(" ", "")
        .replace("-", "")
        .replace("_", "");
  }

  public String value() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CourseCode other)) return false;
    return value.equals(other.value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public String toString() {
    return value;
  }
}
```

## Deduplicator.java (задание 2)

```java
import src.CourseCode;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Удаление дубликатов. Выбрана LinkedHashSet:
// HashSet дал бы уникальность, но потерял порядок,
// TreeSet отсортировал бы, а нам нужен именно порядок первого появления.
public final class Deduplicator {
  private Deduplicator() {
  }

  public static List<CourseCode> unique(List<CourseCode> codes) {
    Set<CourseCode> set = new LinkedHashSet<>();
    for (CourseCode code : codes) {
      if (code == null) throw new NullPointerException("code must not be null");
      set.add(code);
    }
    return List.copyOf(set);
  }

  // Наивный способ "в лоб" - для сравнения сложности: O(n^2) против O(n) у set.
  public static List<CourseCode> uniqueNaive(List<CourseCode> codes) {
    List<CourseCode> result = new ArrayList<>();
    for (CourseCode code : codes) {
      if (!result.contains(code)) {
        result.add(code);
      }
    }
    return result;
  }
}
```

## DeduplicatorTest.java (задание 2)

```java
import org.junit.jupiter.api.Test;
import src.CourseCode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Проверяем и результат, и причину: set убирает дубликаты только потому,
// что CourseCode реализует equals/hashCode.
class DeduplicatorTest {
  private static CourseCode c(String raw) {
    return CourseCode.parse(raw);
  }

  @Test
  void unique_keepsOrderOfFirstAppearance() {
    // Arrange
    List<CourseCode> codes = List.of(
        c("java-101"), c("sql-301"), c("JAVA101"), c("kotlin-201"), c("SQL301"));

    // Act
    List<CourseCode> unique = Deduplicator.unique(codes);

    // Assert: порядок первого появления сохранен
    assertEquals(List.of(c("JAVA101"), c("SQL301"), c("KOTLIN201")), unique);
  }

  @Test
  void unique_collapsesSameNormalizedCodeFromDifferentSources() {
    // Arrange: три записи, которые нормализуются в один код
    List<CourseCode> codes = List.of(
        c("java-101"), c(" JAVA_101 "), c("Java 101"));

    // Act
    List<CourseCode> unique = Deduplicator.unique(codes);

    // Assert
    assertEquals(1, unique.size());
    assertEquals(c("JAVA101"), unique.get(0));
  }

  @Test
  void naiveVersion_givesSameResult() {
    // Arrange
    List<CourseCode> codes = List.of(
        c("java-101"), c("sql-301"), c("JAVA101"));

    // Act
    List<CourseCode> fromSet = Deduplicator.unique(codes);
    List<CourseCode> fromList = Deduplicator.uniqueNaive(codes);

    // Assert: результат одинаковый, разница только в сложности
    assertEquals(fromList, fromSet);
  }

  @Test
  void withoutEqualsAndHashCode_setCannotTellCopiesApart() {
    // Arrange: класс-близнец без equals/hashCode
    List<PlainCode> codes = List.of(
        new PlainCode("JAVA101"), new PlainCode("JAVA101"), new PlainCode("JAVA101"));

    // Act
    long distinct = codes.stream().distinct().count();

    // Assert: identity-сравнение оставляет все три "копии"
    assertEquals(3, distinct, "без equals/hashCode это три разных объекта");
  }

  @Test
  void unique_rejectsNull() {
    // Arrange
    List<CourseCode> codes = new java.util.ArrayList<>();
    codes.add(c("java-101"));
    codes.add(null);

    // Act + Assert: null - ошибка вызывающего, а не элемент
    assertThrows(NullPointerException.class, () -> Deduplicator.unique(codes));
  }

  static final class PlainCode {
    private final String value;

    PlainCode(String value) {
      this.value = value;
    }
  }
}
```

## CourseIndex.java (задание 3)

```java
import src.Course;
import src.CourseId;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

// Индекс CourseId -> Course. LinkedHashMap выбран, чтобы findAll()
// отдавал курсы в порядке добавления.
public class CourseIndex {
  private final Map<CourseId, Course> byId = new LinkedHashMap<>();

  public void add(Course course) {
    if (course == null) throw new NullPointerException("course must not be null");
    // putIfAbsent не перезаписывает молча: результат == null значит "вставили",
    // иначе такой id уже есть и это ошибка вызывающего кода
    Course previous = byId.putIfAbsent(course.id(), course);
    if (previous != null) {
      throw new IllegalStateException("duplicate id: " + course.id());
    }
  }

  public void replace(Course course) {
    // явная команда замены - намерение видно в имени метода
    if (course == null) throw new NullPointerException("course must not be null");
    byId.put(course.id(), course);
  }

  public Optional<Course> find(CourseId id) {
    return Optional.ofNullable(byId.get(id));
  }

  public boolean contains(CourseId id) {
    return byId.containsKey(id);
  }

  // computeIfAbsent: найти или создать одним действием.
  // Фабрика вызывается только при промахе, поэтому дорогое создание
  // объекта не выполняется зря.
  public Course findOrAdd(CourseId id, Supplier<Course> factory) {
    if (id == null) throw new NullPointerException("id must not be null");
    return byId.computeIfAbsent(id, key -> factory.get());
  }

  public int size() {
    return byId.size();
  }
}
```

## CourseIndexTest.java (задание 3)

```java
import org.junit.jupiter.api.Test;
import src.Course;
import src.CourseId;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

// Проверяем, что повторный id не затирает данные молча,
// и что computeIfAbsent вызывает фабрику только при промахе.
class CourseIndexTest {
  private static Course course(long id, String title) {
    return new Course(new CourseId(id), title, 16);
  }

  @Test
  void add_storesCourseAndFindsItById() {
    // Arrange
    CourseIndex index = new CourseIndex();

    // Act
    index.add(course(1, "Java"));

    // Assert
    Optional<Course> found = index.find(new CourseId(1));
    assertTrue(found.isPresent());
    assertEquals("Java", found.get().title());
    assertTrue(index.contains(new CourseId(1)));
  }

  @Test
  void find_returnsEmptyForUnknownId() {
    // Arrange
    CourseIndex index = new CourseIndex();
    index.add(course(1, "Java"));

    // Act + Assert
    assertTrue(index.find(new CourseId(99)).isEmpty());
    assertFalse(index.contains(new CourseId(99)));
  }

  @Test
  void add_duplicateIdThrowsInsteadOfSilentOverwrite() {
    // Arrange
    CourseIndex index = new CourseIndex();
    index.add(course(1, "Java"));

    // Act: тот же id - попытка молча затереть старое значение
    IllegalStateException e = assertThrows(
        IllegalStateException.class,
        () -> index.add(course(1, "Java v2")));

    // Assert: индекс не изменился
    assertEquals("duplicate id: CourseId(1)", e.getMessage());
    assertEquals("Java", index.find(new CourseId(1)).orElseThrow().title());
    assertEquals(1, index.size());
  }

  @Test
  void replace_isExplicitAndOverwrites() {
    // Arrange
    CourseIndex index = new CourseIndex();
    index.add(course(1, "Java"));

    // Act
    index.replace(course(1, "Java v2"));

    // Assert
    assertEquals("Java v2", index.find(new CourseId(1)).orElseThrow().title());
    assertEquals(1, index.size());
  }

  @Test
  void findOrAdd_createsOnlyOnMiss() {
    // Arrange
    CourseIndex index = new CourseIndex();
    AtomicInteger calls = new AtomicInteger();

    // Act: первый вызов создает, второй переиспользует
    Course first = index.findOrAdd(new CourseId(1),
        () -> { calls.incrementAndGet(); return course(1, "Java"); });
    Course second = index.findOrAdd(new CourseId(1),
        () -> { calls.incrementAndGet(); return course(1, "Other"); });

    // Assert: фабрика вызвана один раз, второй раз вернулся тот же объект
    assertEquals(1, calls.get());
    assertSame(first, second);
    assertEquals("Java", second.title());
  }
}
```

## Course.java (задания 3, 6, 9, 10, 12, 13)

```java
package src;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

// Общая модель курса. equals/hashCode намеренно основаны только на id:
// title, duration и popularity могут меняться, а "ключ" курса в коллекциях
// остается стабильным. Именно на этом строится task10.
public class Course {
  private final CourseId id;
  private final String title;
  private final int durationHours;

  // не участвует в равенстве - можно менять, не ломая HashMap/HashSet
  private int popularity;

  private final Set<Tag> tags = new LinkedHashSet<>();

  public Course(CourseId id, String title, int durationHours) {
    if (id == null) throw new NullPointerException("id must not be null");
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("title must not be blank");
    }
    if (durationHours <= 0) {
      throw new IllegalArgumentException("durationHours must be > 0");
    }
    this.id = id;
    this.title = title.trim();
    this.durationHours = durationHours;
  }

  public CourseId id() {
    return id;
  }

  public String title() {
    return title;
  }

  public int durationHours() {
    return durationHours;
  }

  public int popularity() {
    return popularity;
  }

  public void setPopularity(int popularity) {
    this.popularity = popularity;
  }

  public void addTag(Tag tag) {
    if (tag == null) throw new NullPointerException("tag must not be null");
    tags.add(tag);
  }

  public Set<Tag> tags() {
    return Collections.unmodifiableSet(tags);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Course other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "Course[" + id + ", " + title + "]";
  }
}
```

## CourseId.java (задания 3, 6, 8, 10, 11, 12, 13)

```java
package src;

import java.util.Objects;

// Идентификатор курса - value object. equals/hashCode по значению,
// поэтому его безопасно использовать как ключ map/set.
public final class CourseId implements Comparable<CourseId> {
  private final long value;

  public CourseId(long value) {
    if (value <= 0) throw new IllegalArgumentException("id must be > 0");
    this.value = value;
  }

  public long value() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CourseId other)) return false;
    return value == other.value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public int compareTo(CourseId other) {
    return Long.compare(value, other.value);
  }

  @Override
  public String toString() {
    return "CourseId(" + value + ")";
  }
}
```

## Waitlist.java (задание 4)

```java
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;

// Очередь ожидания - строгий FIFO. ArrayDeque выбран вместо LinkedList:
// та же сложность O(1) на концах, но без накладных расходов на узлы-объекты.
public class Waitlist {
  private final Queue<Long> queue = new ArrayDeque<>();

  // offer: вернет false, если вставить нельзя. ArrayDeque не ограничен,
  // но код не должен зависеть от реализации очереди.
  public boolean enqueue(long studentId) {
    return queue.offer(studentId);
  }

  // peek: посмотреть следующего, не извлекая; null при пустой очереди
  public Long nextOrNull() {
    return queue.peek();
  }

  // poll: извлечь следующего; null при пустой очереди
  public Long takeOrNull() {
    return queue.poll();
  }

  // add: та же вставка, но при невозможности бросает IllegalStateException
  public void enqueueOrThrow(long studentId) {
    queue.add(studentId);
  }

  // element: следующего или NoSuchElementException на пустой очереди
  public long nextOrThrow() {
    return queue.element();
  }

  // remove: извлечь или NoSuchElementException на пустой очереди
  public long takeOrThrow() {
    return queue.remove();
  }

  // Optional-обертки прячут null-политику от вызывающего
  public Optional<Long> next() {
    return Optional.ofNullable(queue.peek());
  }

  public Optional<Long> take() {
    return Optional.ofNullable(queue.poll());
  }

  public int size() {
    return queue.size();
  }
}
```

## WaitlistTest.java (задание 4)

```java
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

// FIFO проверяем явно: порядок извлечения совпадает с порядком постановки.
// Пустая очередь - отдельный значимый случай, а не "должно быть пусто".
class WaitlistTest {
  @Test
  void take_returnsStudentsInFifoOrder() {
    // Arrange
    Waitlist waitlist = new Waitlist();
    waitlist.enqueue(100);
    waitlist.enqueue(200);
    waitlist.enqueue(300);

    // Act + Assert
    assertEquals(100L, waitlist.take().orElseThrow());
    assertEquals(200L, waitlist.take().orElseThrow());
    assertEquals(300L, waitlist.take().orElseThrow());
    assertEquals(0, waitlist.size());
  }

  @Test
  void next_peeksWithoutRemoving() {
    // Arrange
    Waitlist waitlist = new Waitlist();
    waitlist.enqueue(100);
    waitlist.enqueue(200);

    // Act
    long first = waitlist.next().orElseThrow();
    long again = waitlist.next().orElseThrow();

    // Assert: peek не меняет очередь
    assertEquals(100, first);
    assertEquals(100, again);
    assertEquals(2, waitlist.size());
  }

  @Test
  void peekOnEmptyQueue_returnsNullNotException() {
    // Arrange
    Waitlist waitlist = new Waitlist();

    // Act + Assert
    assertNull(waitlist.nextOrNull());
    assertTrue(waitlist.next().isEmpty());
  }

  @Test
  void pollOnEmptyQueue_returnsNullNotException() {
    // Arrange
    Waitlist waitlist = new Waitlist();

    // Act + Assert
    assertNull(waitlist.takeOrNull());
    assertTrue(waitlist.take().isEmpty());
  }

  @Test
  void elementAndRemove_throwOnEmptyQueue() {
    // Arrange
    Waitlist waitlist = new Waitlist();

    // Act + Assert: "строгие" версии сигналят об ошибке, а не возвращают null
    assertThrows(NoSuchElementException.class, waitlist::nextOrThrow);
    assertThrows(NoSuchElementException.class, waitlist::takeOrThrow);
  }

  @Test
  void offerAndAdd_insertTheSameWay() {
    // Arrange
    Waitlist waitlist = new Waitlist();

    // Act
    assertTrue(waitlist.enqueue(100));
    waitlist.enqueueOrThrow(200);

    // Assert
    assertEquals(100, waitlist.takeOrThrow());
    assertEquals(200, waitlist.takeOrThrow());
  }
}
```

## ConcurrentModificationDemo.java (задание 5)

```java
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

// Воспроизведение проблемы: enhanced for использует iterator, но список
// меняется напрямую через remove(). Структурная модификация делает
// modCount != expectedModCount, и следующая проверка итератора падает.
public class ConcurrentModificationDemo {
  public static void main(String[] args) {
    List<String> codes = new ArrayList<>(List.of("JAVA101", "SQL301", "KOTLIN201"));

    // Удаляем первый элемент: после сдвига итератор делает еще один next()
    // и замечает изменение modCount. (Удаление последнего просмотренного
    // элемента цикл мог бы просто завершить без проверки - CME не гарантирован.)
    try {
      for (String code : codes) {
        if (code.equals("JAVA101")) {
          codes.remove(code); // ломает итератор
        }
      }
    } catch (ConcurrentModificationException e) {
      System.out.println("caught: " + e.getClass().getSimpleName());
    }
  }
}
```

## SafeRemoval.java (задание 5)

```java
import src.CourseCode;

import java.util.Iterator;
import java.util.List;

// Два правильных способа удалить во время обхода.
// Оба сообщают итератору об изменении, поэтому modCount согласован.
public final class SafeRemoval {
  private SafeRemoval() {
  }

  // iterator.remove: удаляет текущий элемент, не сдвигая итератор сам по себе
  public static void removeWithIterator(List<CourseCode> codes, CourseCode target) {
    Iterator<CourseCode> iterator = codes.iterator();
    while (iterator.hasNext()) {
      if (iterator.next().equals(target)) {
        iterator.remove();
      }
    }
  }

  // removeIf: та же семантика, но ручной цикл не нужен
  public static void removeWithRemoveIf(List<CourseCode> codes, CourseCode target) {
    codes.removeIf(code -> code.equals(target));
  }
}
```

## SafeRemovalTest.java (задание 5)

```java
import org.junit.jupiter.api.Test;
import src.CourseCode;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SafeRemovalTest {
  private static CourseCode c(String raw) {
    return CourseCode.parse(raw);
  }

  private static List<CourseCode> sample() {
    return new ArrayList<>(List.of(c("java-101"), c("sql-301"), c("JAVA101")));
  }

  @Test
  void enhancedForRemoval_throwsConcurrentModificationException() {
    // Arrange
    List<CourseCode> codes = sample();

    // Act + Assert: прямое удаление во время обхода ломает итератор
    assertThrows(ConcurrentModificationException.class, () -> {
      for (CourseCode code : codes) {
        if (code.equals(c("JAVA101"))) {
          codes.remove(code);
        }
      }
    });
  }

  @Test
  void iteratorRemoval_deletesAllMatchesWithoutException() {
    // Arrange: код java-101 встречается дважды (с учетом нормализации)
    List<CourseCode> codes = sample();

    // Act
    SafeRemoval.removeWithIterator(codes, c("JAVA101"));

    // Assert
    assertEquals(List.of(c("SQL301")), codes);
  }

  @Test
  void removeIf_deletesAllMatchesWithoutException() {
    // Arrange
    List<CourseCode> codes = sample();

    // Act
    SafeRemoval.removeWithRemoveIf(codes, c("JAVA101"));

    // Assert
    assertEquals(List.of(c("SQL301")), codes);
  }

  @Test
  void removalOfAbsentElement_changesNothing() {
    // Arrange
    List<CourseCode> codes = sample();

    // Act
    SafeRemoval.removeWithRemoveIf(codes, c("GO-401"));

    // Assert
    assertEquals(sample(), codes);
  }
}
```

## Result.java (задание 6)

```java
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
```

## Page.java (задание 6)

```java
package src;

import java.util.List;
import java.util.Objects;

// Page<T> - вторая конкретизация generic-идеи: неизменяемая страница выдачи.
// Инварианты проверяются в конструкторе, список копируется,
// чтобы наружу не утекала изменяемая внутренняя коллекция.
public final class Page<T> {
  private final List<T> items;
  private final int pageNumber;
  private final int pageSize;
  private final long totalItems;

  public Page(List<T> items, int pageNumber, int pageSize, long totalItems) {
    if (items == null) throw new NullPointerException("items must not be null");
    if (pageNumber < 0) throw new IllegalArgumentException("pageNumber must be >= 0");
    if (pageSize <= 0) throw new IllegalArgumentException("pageSize must be > 0");
    if (totalItems < items.size()) {
      throw new IllegalArgumentException("totalItems cannot be less than items on the page");
    }
    this.items = List.copyOf(items);
    this.pageNumber = pageNumber;
    this.pageSize = pageSize;
    this.totalItems = totalItems;
  }

  public List<T> items() {
    return items;
  }

  public int pageNumber() {
    return pageNumber;
  }

  public int pageSize() {
    return pageSize;
  }

  public long totalItems() {
    return totalItems;
  }

  public int totalPages() {
    return (int) ((totalItems + pageSize - 1) / pageSize);
  }

  public boolean hasNext() {
    return pageNumber + 1 < totalPages();
  }

  public boolean isEmpty() {
    return items.isEmpty();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Page<?> other)) return false;
    return pageNumber == other.pageNumber
        && pageSize == other.pageSize
        && totalItems == other.totalItems
        && items.equals(other.items);
  }

  @Override
  public int hashCode() {
    return Objects.hash(items, pageNumber, pageSize, totalItems);
  }

  @Override
  public String toString() {
    return "Page[" + pageNumber + "/" + totalPages() + ", items=" + items.size() + "]";
  }
}
```

## ResultTest.java (задание 6)

```java
import org.junit.jupiter.api.Test;
import src.Course;
import src.CourseId;
import src.Page;
import src.Result;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {
  @Test
  void ok_carriesTypedValue() {
    // Arrange: конкретизация Result<Course>
    Course course = new Course(new CourseId(1), "Java", 16);

    // Act
    Result<Course> result = Result.ok(course);

    // Assert
    assertTrue(result.isOk());
    assertSame(course, result.value());
    assertTrue(result.toOptional().isPresent());
  }

  @Test
  void error_carriesMessageAndHasNoValue() {
    // Arrange: конкретизация Result<CourseId>
    Result<CourseId> result = Result.error("course not found");

    // Act + Assert
    assertTrue(result.isError());
    assertEquals("course not found", result.error());
    assertThrows(IllegalStateException.class, result::value);
    assertTrue(result.toOptional().isEmpty());
  }

  @Test
  void factories_rejectMissingParts() {
    // Arrange + Act + Assert: инварианты проверяются фабриками
    assertThrows(NullPointerException.class, () -> Result.ok(null));
    assertThrows(IllegalArgumentException.class, () -> Result.error("  "));
  }

  @Test
  void map_transformsOnlyOkBranch() {
    // Arrange
    Result<Course> ok = Result.ok(new Course(new CourseId(1), "Java", 16));
    Result<Course> error = Result.error("boom");

    // Act
    Result<String> title = ok.map(c -> c.title().toUpperCase());
    Result<String> stillError = error.map(c -> c.title().toUpperCase());

    // Assert
    assertEquals("JAVA", title.value());
    assertTrue(stillError.isError());
    assertEquals("boom", stillError.error());
  }

  @Test
  void flatMap_doesNotWrapTwice() {
    // Arrange
    Result<Course> source = Result.ok(new Course(new CourseId(1), "Java", 16));

    // Act: функция сама возвращает Result
    Result<Integer> hours = source.flatMap(c -> Result.ok(c.durationHours()));

    // Assert
    assertEquals(16, hours.value());
  }

  @Test
  void equals_comparesStateNotIdentity() {
    // Arrange
    Result<Integer> a = Result.ok(5);
    Result<Integer> b = Result.ok(5);

    // Act + Assert
    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertNotEquals(a, Result.error("5"));
  }

  @Test
  void page_isAnImmutableView() {
    // Arrange
    List<String> raw = new java.util.ArrayList<>(List.of("Java", "Kotlin", "SQL"));

    // Act
    Page<String> page = new Page<>(raw, 0, 2, 5);
    raw.add("Go"); // изменение источника не должно просочиться

    // Assert: список скопирован в конструкторе
    assertEquals(List.of("Java", "Kotlin", "SQL"), page.items());
    assertThrows(UnsupportedOperationException.class, () -> page.items().add("Rust"));
    assertEquals(3, page.totalPages());
    assertTrue(page.hasNext());
  }

  // Компиляторная защита: это не соберется, поэтому оставлено комментарием.
  // Result<Course> courses = Result.ok(new Course(...));
  // Result<CourseId> ids = courses; // incompatible types: Result<Course> -> Result<CourseId>
}
```

## GenericLists.java (задание 7)

```java
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
```

## GenericListsTest.java (задание 7)

```java
import org.junit.jupiter.api.Test;
import src.GenericLists;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class GenericListsTest {
  @Test
  void firstOrThrow_returnsHeadForAnyType() {
    // Arrange: один и тот же метод работает и для String, и для Integer
    List<String> strings = List.of("a", "b");
    List<Integer> numbers = List.of(1, 2, 3);

    // Act + Assert: параметр типа выводится на месте вызова
    assertEquals("a", GenericLists.firstOrThrow(strings));
    assertEquals(1, GenericLists.firstOrThrow(numbers));
  }

  @Test
  void firstOrThrow_rejectsEmptyList() {
    // Arrange
    List<String> empty = List.of();

    // Act + Assert
    assertThrows(NoSuchElementException.class, () -> GenericLists.firstOrThrow(empty));
  }

  @Test
  void swap_exchangesElementsInPlace() {
    // Arrange
    List<String> list = new ArrayList<>(List.of("a", "b", "c"));

    // Act
    GenericLists.swap(list, 0, 2);

    // Assert
    assertEquals(List.of("c", "b", "a"), list);
  }

  @Test
  void swap_rejectsOutOfRangeIndex() {
    // Arrange
    List<String> list = new ArrayList<>(List.of("a"));

    // Act + Assert
    assertThrows(IndexOutOfBoundsException.class, () -> GenericLists.swap(list, 0, 1));
  }

  @Test
  void indexBy_buildsMapByKey() {
    // Arrange
    List<String> courses = List.of("Java", "Kotlin", "SQL");

    // Act: ключ - длина названия
    Map<Integer, String> byLength =
        GenericLists.indexBy(courses, String::length);

    // Assert
    assertEquals("Java", byLength.get(4));
    assertEquals("SQL", byLength.get(3));
    assertEquals(3, byLength.size());
  }

  @Test
  void indexBy_conflictThrowsInsteadOfSilentOverwrite() {
    // Arrange: "Java" и "Rust" дают одинаковую длину
    List<String> courses = List.of("Java", "Kotlin", "Rust");

    // Act
    IllegalStateException e = assertThrows(
        IllegalStateException.class,
        () -> GenericLists.indexBy(courses, String::length));

    // Assert: видно, какой ключ и какие значения столкнулись
    assertTrue(e.getMessage().contains("duplicate key 4"));
  }

  @Test
  void indexBy_withMerge_resolvesConflictExplicitly() {
    // Arrange
    List<String> courses = List.of("Java", "Kotlin", "Rust");

    // Act: стратегия - оставить более длинное название
    Map<Integer, String> resolved = GenericLists.indexBy(
        courses, String::length, (a, b) -> a.length() >= b.length() ? a : b);

    // Assert
    assertEquals("Kotlin", resolved.get(6));
    assertEquals(2, resolved.size()); // длины 4 и 6
  }
}
```

## Comparables.java (задание 8)

```java
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
```

## ComparablesTest.java (задание 8)

```java
import org.junit.jupiter.api.Test;
import src.Comparables;
import src.CourseId;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class ComparablesTest {
  @Test
  void max_worksForInteger() {
    // Arrange
    List<Integer> numbers = List.of(3, 7, 7, 2, 5);

    // Act + Assert
    assertEquals(7, Comparables.max(numbers));
    assertEquals(2, Comparables.min(numbers));
  }

  @Test
  void max_worksForString() {
    // Arrange: лексикографический порядок
    List<String> words = List.of("Java", "Kotlin", "SQL");

    // Act + Assert
    assertEquals("SQL", Comparables.max(words));
    assertEquals("Java", Comparables.min(words));
  }

  @Test
  void max_worksForCustomValueObject() {
    // Arrange: CourseId реализует Comparable<CourseId>
    List<CourseId> ids = List.of(new CourseId(10), new CourseId(3), new CourseId(42));

    // Act + Assert
    assertEquals(new CourseId(42), Comparables.max(ids));
    assertEquals(new CourseId(3), Comparables.min(ids));
  }

  @Test
  void max_acceptsSubtypeWhoseCompareToComesFromSupertype() {
    // Arrange: Derived наследует compareTo от Base, то есть Comparable<Base>
    List<Derived> items = List.of(new Derived(1), new Derived(5), new Derived(3));

    // Act: граница ? super T разрешает это, Comparable<T> - нет
    Derived max = Comparables.max(items);

    // Assert
    assertEquals(5, max.rank);
  }

  @Test
  void max_rejectsEmptyList() {
    // Act + Assert
    assertThrows(NoSuchElementException.class, () -> Comparables.max(List.of()));
  }

  static class Base implements Comparable<Base> {
    final int rank;

    Base(int rank) {
      this.rank = rank;
    }

    @Override
    public int compareTo(Base other) {
      return Integer.compare(rank, other.rank);
    }
  }

  static final class Derived extends Base {
    Derived(int rank) {
      super(rank);
    }
  }
}
```

## CollectionCopies.java (задание 9)

```java
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
```

## PecsTest.java (задание 9)

```java
import org.junit.jupiter.api.Test;
import src.CollectionCopies;
import src.Course;
import src.CourseId;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PecsTest {
  @Test
  void copy_fromSubtypeListToSupertypeList() {
    // Arrange: producer List<Integer>, consumer List<Number>
    List<Integer> integers = List.of(1, 2, 3);
    List<Number> numbers = new ArrayList<>();

    // Act
    CollectionCopies.copy(integers, numbers);

    // Assert
    assertEquals(List.of(1, 2, 3), numbers);
  }

  @Test
  void copy_fromCourseListToObjectList() {
    // Arrange: конкретный тип -> более общий
    List<Course> courses = List.of(new Course(new CourseId(1), "Java", 16));
    List<Object> objects = new ArrayList<>();

    // Act
    CollectionCopies.copy(courses, objects);

    // Assert
    assertEquals(1, objects.size());
    assertSame(courses.get(0), objects.get(0));
  }

  @Test
  void superList_allowsWritingEvenIfConcreteTypeUnknown() {
    // Arrange: List<? super Course> и List<? super Integer>
    List<Object> storage = new ArrayList<>();
    List<? super Course> consumer = storage;

    // Act: в consumer можно класть Course, тип элемента consumer - предок Course
    consumer.add(new Course(new CourseId(2), "Kotlin", 24));
    consumer.add(new Course(new CourseId(3), "SQL", 8));

    // Assert
    assertEquals(2, storage.size());
  }

  @Test
  void extendsList_allowsReadingAsTopTypeOnly() {
    // Arrange: List<? extends Course>
    List<? extends Course> producer =
        List.of(new Course(new CourseId(4), "Go", 12));

    // Act: прочитать можно гарантированно как Course
    Course first = producer.get(0);

    // Assert
    assertEquals("Go", first.title());
    // producer.add(new Course(...)) не компилируется: тип элемента - какой-то
    // неизвестный подтип Course, и добавить конкретный Course нельзя.
  }

  // Компиляторная защита - оставлено комментарием:
  // List<? extends Course> producer = new ArrayList<Course>();
  // producer.add(new Course(new CourseId(9), "Rust", 10)); // compile error
}
```

## KeyContractDemo.java (задание 10)

```java
import src.Course;
import src.CourseId;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Контракт ключа: если поле не участвует в equals/hashCode, его изменение
// не ломает поиск в HashSet/HashMap.
public class KeyContractDemo {
  public static void main(String[] args) {
    Course course = new Course(new CourseId(1), "Java", 16);
    course.setPopularity(10);

    Set<Course> set = new HashSet<>();
    set.add(course);
    Map<Course, String> map = new HashMap<>();
    map.put(course, "spring-2026");

    // меняем popularity - поле вне equals/hashCode
    course.setPopularity(999);

    System.out.println("set.contains = " + set.contains(course));
    System.out.println("map.get       = " + map.get(course));
  }
}
```

## BadCourse.java (задание 10)

```java
import java.util.Objects;

// Антипример: hashCode включает изменяемое поле popularity.
// Пока объект лежит в HashSet/HashMap, его хеш меняется - корзина, куда
// объект положили, перестает совпадать с текущим хешем.
public class BadCourse {
  private final long id;
  private int popularity;

  public BadCourse(long id, int popularity) {
    this.id = id;
    this.popularity = popularity;
  }

  public void setPopularity(int popularity) {
    this.popularity = popularity;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BadCourse other)) return false;
    return id == other.id && popularity == other.popularity;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, popularity);
  }
}
```

## BrokenKeyDemo.java (задание 10)

```java
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Воспроизведение проблемы: изменяемое поле попало в hashCode.
// После setPopularity тот же объект "теряется" в set и map.
public class BrokenKeyDemo {
  public static void main(String[] args) {
    BadCourse course = new BadCourse(1, 10);

    Set<BadCourse> set = new HashSet<>();
    set.add(course);
    Map<BadCourse, String> map = new HashMap<>();
    map.put(course, "spring-2026");

    System.out.println("before: set.contains = " + set.contains(course)); // true
    course.setPopularity(999); // hash изменился, объект остался в старой корзине
    System.out.println("after:  set.contains = " + set.contains(course)); // false
    System.out.println("after:  map.get       = " + map.get(course));      // null
    System.out.println("after:  set.size      = " + set.size());           // 1 (не пропал!)
  }
}
```

## KeyContractTest.java (задание 10)

```java
import org.junit.jupiter.api.Test;
import src.Course;
import src.CourseId;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class KeyContractTest {
  @Test
  void changingNonEqualityField_keepsLookupWorking() {
    // Arrange
    Course course = new Course(new CourseId(1), "Java", 16);
    Set<Course> set = new HashSet<>();
    set.add(course);
    Map<Course, String> map = new HashMap<>();
    map.put(course, "spring-2026");

    // Act: popularity не входит в equals/hashCode
    course.setPopularity(999);

    // Assert: объект по-прежнему находится
    assertTrue(set.contains(course));
    assertEquals("spring-2026", map.get(course));
  }

  @Test
  void equalCourses_shareHashCode() {
    // Arrange: два разных объекта с одним id
    Course a = new Course(new CourseId(7), "Java", 16);
    Course b = new Course(new CourseId(7), "Java Advanced", 32);

    // Act + Assert: equals по id => одинаковый hashCode
    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertEquals(1, new HashSet<>(java.util.List.of(a, b)).size());
  }

  @Test
  void mutableHashField_breaksSetLookup() {
    // Arrange
    BadCourse course = new BadCourse(1, 10);
    Set<BadCourse> set = new HashSet<>();
    set.add(course);

    // Act
    course.setPopularity(999);

    // Assert: объект остался в set, но найти его нельзя
    assertEquals(1, set.size(), "element is still physically inside");
    assertFalse(set.contains(course), "but hash no longer matches its bucket");
  }

  @Test
  void mutableHashField_breaksMapLookup() {
    // Arrange
    BadCourse course = new BadCourse(1, 10);
    Map<BadCourse, String> map = new HashMap<>();
    map.put(course, "spring-2026");

    // Act
    course.setPopularity(999);

    // Assert
    assertNull(map.get(course));
    assertEquals(1, map.size(), "entry is still inside, just unreachable");
  }

  @Test
  void keyShouldBeImmutable() {
    // Arrange: корректный ключ - неизменяемый CourseId
    CourseId id = new CourseId(3);
    Map<CourseId, String> map = new HashMap<>();
    map.put(id, "Kotlin");

    // Act: значение меняется, ключ - нет
    map.put(id, "Kotlin v2");

    // Assert
    assertEquals("Kotlin v2", map.get(new CourseId(3)));
  }
}
```

## Leaderboard.java (задание 11)

```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

// Сценарий "отсортированный leaderboard": нужен порядок по счету.
// Выбор - TreeMap (красно-черное дерево): вставка/поиск O(log n),
// а первый/последний элемент и "топ-N" достаются без полной сортировки.
public class Leaderboard {
  // обратный порядок: больший счет - первым
  private final NavigableMap<Integer, List<String>> byScore =
      new TreeMap<>(Collections.reverseOrder());

  public void score(String student, int points) {
    byScore.computeIfAbsent(points, k -> new ArrayList<>()).add(student);
  }

  public List<String> top(int n) {
    List<String> result = new ArrayList<>();
    for (Map.Entry<Integer, List<String>> entry : byScore.entrySet()) {
      for (String student : entry.getValue()) {
        if (result.size() == n) return List.copyOf(result);
        result.add(student);
      }
    }
    return List.copyOf(result);
  }

  public int bestScore() {
    return byScore.isEmpty() ? -1 : byScore.firstKey();
  }

  // LinkedHashMap лишь для наглядного снимка "как хранится"
  public Map<Integer, List<String>> snapshot() {
    return new LinkedHashMap<>(byScore);
  }
}
```

## RangeIndex.java (задание 11)

```java
import src.CourseId;

import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

// Сценарий "диапазонный поиск": нужен поиск по интервалу ключей.
// NavigableMap (TreeMap) дает subMap/headMap/tailMap за O(log n + k),
// чего не умеют HashMap (нет порядка) и ArrayList (только линейный поиск).
public class RangeIndex {
  private final NavigableMap<CourseId, String> byId = new TreeMap<>();

  public void put(CourseId id, String title) {
    byId.put(id, title);
  }

  // все курсы с id в [from, to] включительно
  public SortedMap<CourseId, String> range(long from, long to) {
    if (from > to) throw new IllegalArgumentException("from must be <= to");
    return byId.subMap(new CourseId(from), true, new CourseId(to), true);
  }

  public int size() {
    return byId.size();
  }
}
```

## CollectionChoiceTest.java (задание 11)

```java
import org.junit.jupiter.api.Test;
import src.CourseId;

import java.util.List;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.*;

class CollectionChoiceTest {
  @Test
  void leaderboard_ordersByScoreDescending() {
    // Arrange
    Leaderboard board = new Leaderboard();

    // Act
    board.score("Ann", 90);
    board.score("Bob", 70);
    board.score("Cid", 90);
    board.score("Dan", 50);

    // Assert: тот же счет - в порядке добавления
    assertEquals(List.of("Ann", "Cid", "Bob", "Dan"), board.top(4));
    assertEquals(90, board.bestScore());
    assertEquals(List.of("Ann", "Cid"), board.top(2));
  }

  @Test
  void range_returnsOnlyIdsInsideIntervalInOrder() {
    // Arrange
    RangeIndex index = new RangeIndex();
    index.put(new CourseId(10), "A");
    index.put(new CourseId(20), "B");
    index.put(new CourseId(30), "C");
    index.put(new CourseId(40), "D");

    // Act
    SortedMap<CourseId, String> window = index.range(15, 35);

    // Assert: O(log n + k), границы включительно, порядок по ключу
    assertEquals(List.of(new CourseId(20), new CourseId(30)),
        List.copyOf(window.keySet()));
  }

  @Test
  void range_withInvertedBoundsIsRejected() {
    // Arrange
    RangeIndex index = new RangeIndex();

    // Act + Assert
    assertThrows(IllegalArgumentException.class, () -> index.range(30, 10));
  }

  @Test
  void leaderboard_topMoreThanPresent_returnsEveryone() {
    // Arrange
    Leaderboard board = new Leaderboard();
    board.score("Ann", 10);

    // Act + Assert: просим больше, чем есть - не падаем
    assertEquals(List.of("Ann"), board.top(99));
  }
}
```

## Repository.java (задание 12)

```java
package src;

import java.util.List;
import java.util.Optional;

// Обобщенный контракт хранилища. ID и T - параметры типа,
// поэтому один и тот же интерфейс годится и для Course, и для Tag.
public interface Repository<ID, T> {
  // семантика повторного save: upsert (заменить существующее)
  void save(T entity);

  Optional<T> findById(ID id);

  List<T> findAll();

  boolean deleteById(ID id);
}
```

## InMemoryRepository.java (задание 12)

```java
package src;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

// In-memory реализация Repository.
// - внутренний map не отдается наружу: findAll возвращает неизменяемую копию;
// - null не принимается ни на вход, ни как id;
// - повторный save c тем же id = замена (upsert), размер не растет.
public class InMemoryRepository<ID, T> implements Repository<ID, T> {
  private final Map<ID, T> store = new LinkedHashMap<>();
  private final Function<T, ID> idFn;

  public InMemoryRepository(Function<T, ID> idFn) {
    if (idFn == null) throw new NullPointerException("idFn must not be null");
    this.idFn = idFn;
  }

  @Override
  public void save(T entity) {
    if (entity == null) throw new NullPointerException("entity must not be null");
    ID id = idFn.apply(entity);
    if (id == null) throw new NullPointerException("id must not be null");
    store.put(id, entity);
  }

  @Override
  public Optional<T> findById(ID id) {
    if (id == null) throw new NullPointerException("id must not be null");
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public List<T> findAll() {
    return List.copyOf(store.values());
  }

  @Override
  public boolean deleteById(ID id) {
    if (id == null) throw new NullPointerException("id must not be null");
    return store.remove(id) != null;
  }

  public int size() {
    return store.size();
  }
}
```

## RepositoryTest.java (задание 12)

```java
import org.junit.jupiter.api.Test;
import src.Course;
import src.CourseId;
import src.InMemoryRepository;
import src.Repository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryTest {
  private static Repository<CourseId, Course> repo() {
    return new InMemoryRepository<>(c -> c.id());
  }

  private static Course course(CourseId id, String title) {
    return new Course(id, title, 16);
  }

  @Test
  void saveThenFindById_returnsSameEntity() {
    // Arrange
    Repository<CourseId, Course> repository = repo();
    Course course = course(new CourseId(1), "Java");

    // Act
    repository.save(course);
    Optional<Course> found = repository.findById(new CourseId(1));

    // Assert
    assertTrue(found.isPresent());
    assertSame(course, found.get());
  }

  @Test
  void findById_unknownId_returnsEmpty() {
    // Arrange
    Repository<CourseId, Course> repository = repo();

    // Act + Assert
    assertTrue(repository.findById(new CourseId(99)).isEmpty());
  }

  @Test
  void repeatedSaveWithSameId_replacesWithoutGrowing() {
    // Arrange: семантика upsert
    Repository<CourseId, Course> repository = repo();
    repository.save(course(new CourseId(1), "Java"));
    repository.save(course(new CourseId(1), "Java v2"));

    // Act
    Optional<Course> found = repository.findById(new CourseId(1));

    // Assert: новое значение, размер не вырос
    assertEquals("Java v2", found.orElseThrow().title());
    assertEquals(1, repository.findAll().size());
  }

  @Test
  void findAll_returnsImmutableSnapshotOfCurrentState() {
    // Arrange
    Repository<CourseId, Course> repository = repo();
    repository.save(course(new CourseId(1), "Java"));

    // Act
    List<Course> snapshot = repository.findAll();
    repository.save(course(new CourseId(2), "Kotlin"));

    // Assert: снимок не увидел второй save и его нельзя изменить
    assertEquals(1, snapshot.size());
    assertThrows(UnsupportedOperationException.class, () -> snapshot.add(null));
  }

  @Test
  void deleteById_removesOnlyThatEntity() {
    // Arrange
    Repository<CourseId, Course> repository = repo();
    repository.save(course(new CourseId(1), "Java"));
    repository.save(course(new CourseId(2), "Kotlin"));

    // Act
    boolean removed = repository.deleteById(new CourseId(1));

    // Assert
    assertTrue(removed);
    assertTrue(repository.findById(new CourseId(2)).isPresent());
    assertEquals(1, repository.findAll().size());
    assertFalse(repository.deleteById(new CourseId(1)), "second delete is a miss");
  }

  @Test
  void nullIsRejectedEverywhere() {
    // Arrange
    Repository<CourseId, Course> repository = repo();

    // Act + Assert: null - ошибка программиста, а не данные
    assertThrows(NullPointerException.class, () -> repository.save(null));
    assertThrows(NullPointerException.class, () -> repository.findById(null));
    assertThrows(NullPointerException.class, () -> repository.deleteById(null));
  }
}
```

## InvertedIndex.java (задание 13)

```java
import src.Course;
import src.CourseId;
import src.Tag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

// Обратный индекс: по тегу - множество курсов. Поиск по тегу O(1),
// а не полный перебор курсов (как было бы с List или одним Map<Course, Set<Tag>>).
public class InvertedIndex {
  private final Map<Tag, Set<CourseId>> index = new HashMap<>();

  public void add(Course course) {
    if (course == null) throw new NullPointerException("course must not be null");
    for (Tag tag : course.tags()) {
      index.computeIfAbsent(tag, k -> new LinkedHashSet<>()).add(course.id());
    }
  }

  // удаление курса: id уходит из всех множеств; пустой set сразу удаляется
  public void remove(CourseId id, Set<Tag> tags) {
    if (id == null || tags == null) throw new NullPointerException("id and tags must not be null");
    for (Tag tag : tags) {
      Set<CourseId> ids = index.get(tag);
      if (ids == null) continue;
      ids.remove(id);
      if (ids.isEmpty()) {
        index.remove(tag); // инвариант: в индексе не остается пустых set
      }
    }
  }

  // пересечение нескольких тегов: курс подходит, если есть ВСЕ теги
  public Set<CourseId> coursesWithAll(Set<Tag> tags) {
    if (tags.isEmpty()) return Set.of();
    Set<CourseId> result = null;
    for (Tag tag : tags) {
      Set<CourseId> ids = index.get(tag);
      if (ids == null) return Set.of();
      if (result == null) {
        result = new HashSet<>(ids);
      } else {
        result.retainAll(ids);
      }
    }
    return Set.copyOf(result);
  }

  // объединение нескольких тегов: курс подходит, если есть ХОТЯ БЫ один
  public Set<CourseId> coursesWithAny(Set<Tag> tags) {
    Set<CourseId> result = new HashSet<>();
    for (Tag tag : tags) {
      Set<CourseId> ids = index.get(tag);
      if (ids != null) {
        result.addAll(ids);
      }
    }
    return Set.copyOf(result);
  }

  public int tagCount() {
    return index.size();
  }
}
```

## Tag.java (задание 13)

```java
package src;

import java.util.Locale;

// Тег - тоже value object: "Backend", "backend" и " BACKEND " это один тег.
// Поэтому нормализация и equals/hashCode обязательны (см. task13).
public final class Tag implements Comparable<Tag> {
  private final String name;

  private Tag(String name) {
    this.name = name;
  }

  public static Tag of(String raw) {
    if (raw == null) throw new NullPointerException("tag must not be null");
    String normalized = raw.trim().toLowerCase(Locale.ROOT);
    if (normalized.isEmpty()) {
      throw new IllegalArgumentException("tag must not be blank");
    }
    return new Tag(normalized);
  }

  public String name() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Tag other)) return false;
    return name.equals(other.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public int compareTo(Tag other) {
    return name.compareTo(other.name);
  }

  @Override
  public String toString() {
    return "#" + name;
  }
}
```

## InvertedIndexTest.java (задание 13)

```java
import org.junit.jupiter.api.Test;
import src.Course;
import src.CourseId;
import src.Tag;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class InvertedIndexTest {
  private static Course course(long id, String title, String... tagNames) {
    Course course = new Course(new CourseId(id), title, 16);
    for (String name : tagNames) {
      course.addTag(Tag.of(name));
    }
    return course;
  }

  @Test
  void add_indexesCourseUnderEveryTag() {
    // Arrange
    InvertedIndex index = new InvertedIndex();
    Course java = course(1, "Java", "backend", "jvm");

    // Act
    index.add(java);

    // Assert
    assertEquals(2, index.tagCount());
    assertEquals(Set.of(new CourseId(1)),
        index.coursesWithAll(Set.of(Tag.of("backend"))));
    assertEquals(Set.of(new CourseId(1)),
        index.coursesWithAll(Set.of(Tag.of("JVM"))));
  }

  @Test
  void intersection_requiresAllTags() {
    // Arrange
    InvertedIndex index = new InvertedIndex();
    index.add(course(1, "Java", "backend", "jvm"));
    index.add(course(2, "Kotlin", "backend", "android"));
    index.add(course(3, "SQL", "data"));

    // Act
    Set<CourseId> backend = index.coursesWithAll(Set.of(Tag.of("backend")));
    Set<CourseId> backendAndJvm =
        index.coursesWithAll(Set.of(Tag.of("backend"), Tag.of("jvm")));

    // Assert
    assertEquals(Set.of(new CourseId(1), new CourseId(2)), backend);
    assertEquals(Set.of(new CourseId(1)), backendAndJvm);
  }

  @Test
  void intersection_withUnknownTag_returnsEmpty() {
    // Arrange
    InvertedIndex index = new InvertedIndex();
    index.add(course(1, "Java", "backend"));

    // Act
    Set<CourseId> result = index.coursesWithAll(Set.of(Tag.of("backend"), Tag.of("ai")));

    // Assert
    assertTrue(result.isEmpty());
  }

  @Test
  void union_returnsCoursesHavingAnyTag() {
    // Arrange
    InvertedIndex index = new InvertedIndex();
    index.add(course(1, "Java", "backend", "jvm"));
    index.add(course(2, "Kotlin", "android"));
    index.add(course(3, "SQL", "data"));

    // Act
    Set<CourseId> result = index.coursesWithAny(Set.of(Tag.of("jvm"), Tag.of("data")));

    // Assert
    assertEquals(Set.of(new CourseId(1), new CourseId(3)), result);
  }

  @Test
  void remove_stripsIdFromAllTags_andDropsEmptySets() {
    // Arrange
    InvertedIndex index = new InvertedIndex();
    Course java = course(1, "Java", "backend", "jvm");
    index.add(java);
    index.add(course(2, "Kotlin", "backend"));

    // Act: удаляем курс 1 из обоих его тегов
    index.remove(new CourseId(1), Set.of(Tag.of("backend"), Tag.of("jvm")));

    // Assert: у "jvm" не осталось курсов - set пропал из индекса
    assertEquals(1, index.tagCount(), "empty jvm set must be removed");
    assertEquals(Set.of(new CourseId(2)),
        index.coursesWithAll(Set.of(Tag.of("backend"))));
    assertTrue(index.coursesWithAll(Set.of(Tag.of("jvm"))).isEmpty());
  }

  @Test
  void remove_withTagNotInIndex_stillWorks() {
    // Arrange
    InvertedIndex index = new InvertedIndex();
    index.add(course(1, "Java", "backend"));

    // Act: удаляем курс с тегом, которого в индексе нет
    index.remove(new CourseId(1), Set.of(Tag.of("missing")));

    // Assert: ничего не упало, данные целы
    assertEquals(1, index.tagCount());
    assertEquals(Set.of(new CourseId(1)),
        index.coursesWithAll(Set.of(Tag.of("backend"))));
  }
}
```

## LruCache.java (задание 14)

```java
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
```

## LruCacheTest.java (задание 14)

```java
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
```

## PrerequisiteGraph.java (задание 15)

```java
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Generic-модель графа зависимостей: курс -> его prerequisites.
// Соседние множества LinkedHashSet, чтобы перебор был детерминированным
// (порядок вставки, а не хеш-порядок).
public class PrerequisiteGraph<V> {
  private final Map<V, Set<V>> prereqOf = new HashMap<>();
  private final Map<V, Set<V>> dependents = new HashMap<>();
  private final List<V> insertionOrder = new java.util.ArrayList<>();

  // регистрирует вершину явно; повторный вызов - no-op
  public void register(V course) {
    if (course == null) throw new NullPointerException("course must not be null");
    prereqOf.computeIfAbsent(course, k -> {
      insertionOrder.add(k);
      return new LinkedHashSet<>();
    });
    dependents.computeIfAbsent(course, k -> new LinkedHashSet<>());
  }

  // course = dependsOn + prereq = ... несуществующие вершины создаются сами
  public void addPrerequisite(V course, V prereq) {
    register(course);
    register(prereq);
    prereqOf.get(course).add(prereq);
    dependents.get(prereq).add(course);
  }

  public boolean contains(V course) {
    return prereqOf.containsKey(course);
  }

  public int vertexCount() {
    return insertionOrder.size();
  }

  // Топологическая сортировка (Кан). Если граф зациклен - бросаем с путем цикла.
  // Детерминизм: и очередь стартов, и перебор соседей - в порядке вставки,
  // поэтому одинаковый ввод дает одинаковый результат.
  public List<V> order() {
    Map<V, Integer> indegree = new HashMap<>();
    for (V v : insertionOrder) indegree.put(v, 0);
    for (Map.Entry<V, Set<V>> entry : prereqOf.entrySet()) {
      indegree.put(entry.getKey(), entry.getValue().size());
    }

    Deque<V> ready = new ArrayDeque<>();
    for (V v : insertionOrder) {
      if (indegree.get(v) == 0) ready.add(v);
    }

    List<V> result = new java.util.ArrayList<>();
    while (!ready.isEmpty()) {
      V v = ready.poll();
      result.add(v);
      for (V dependent : dependents.getOrDefault(v, Set.of())) {
        int left = indegree.get(dependent) - 1;
        indegree.put(dependent, left);
        if (left == 0) ready.add(dependent);
      }
    }

    if (result.size() != insertionOrder.size()) {
      throw new IllegalStateException(
          "cycle detected: " + findCycle().orElse(List.of()));
    }
    return List.copyOf(result);
  }

  // Диагностический путь цикла: three-color DFS. Возвращает [v1, v2, ..., v1],
  // при отсутствии цикла - пусто. Путь детерминирован порядком вставки.
  public java.util.Optional<List<V>> findCycle() {
    Map<V, Integer> color = new HashMap<>(); // 0 white, 1 gray, 2 black
    for (V v : insertionOrder) color.put(v, 0);

    for (V start : insertionOrder) {
      if (color.get(start) != 0) continue;

      Deque<V> stack = new ArrayDeque<>();
      Deque<java.util.Iterator<V>> iterators = new ArrayDeque<>();
      stack.push(start);
      color.put(start, 1);
      iterators.push(prereqOf.getOrDefault(start, Set.of()).iterator());

      while (!stack.isEmpty()) {
        V v = stack.peek();
        java.util.Iterator<V> it = iterators.peek();
        if (it.hasNext()) {
          V next = it.next();
          int nextColor = color.getOrDefault(next, 0);
          if (nextColor == 1) {
            // стек лежит вершиной вверх: верх - текущая, низ - корень.
            // Разворачиваем в путь "корень -> текущая" и вырезаем цикл.
            List<V> path = new java.util.ArrayList<>(stack);
            java.util.Collections.reverse(path);
            int from = path.indexOf(next);
            List<V> cycle = new java.util.ArrayList<>(
                path.subList(from, path.size()));
            cycle.add(next);
            return java.util.Optional.of(cycle);
          }
          if (nextColor == 0) {
            color.put(next, 1);
            stack.push(next);
            iterators.push(prereqOf.getOrDefault(next, Set.of()).iterator());
          }
        } else {
          color.put(v, 2);
          stack.pop();
          iterators.pop();
        }
      }
    }
    return java.util.Optional.empty();
  }
}
```

## PrerequisiteGraphTest.java (задание 15)

```java
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrerequisiteGraphTest {
  @Test
  void order_respectsAllDependencies() {
    // Arrange: Java <- (Base, OOP), OOP <- Base
    PrerequisiteGraph<String> graph = new PrerequisiteGraph<>();
    graph.addPrerequisite("Java", "Base");
    graph.addPrerequisite("Java", "OOP");
    graph.addPrerequisite("OOP", "Base");

    // Act
    List<String> order = graph.order();

    // Assert: prerequisite всегда раньше курса
    assertTrue(order.indexOf("Base") < order.indexOf("Java"));
    assertTrue(order.indexOf("Base") < order.indexOf("OOP"));
    assertTrue(order.indexOf("OOP") < order.indexOf("Java"));
    assertEquals(3, order.size());
  }

  @Test
  void order_isDeterministicForTheSameInput() {
    // Arrange: два одинаковых графа, построенных одинаково
    PrerequisiteGraph<String> first = new PrerequisiteGraph<>();
    first.addPrerequisite("C", "A");
    first.addPrerequisite("C", "B");
    first.addPrerequisite("B", "A");

    PrerequisiteGraph<String> second = new PrerequisiteGraph<>();
    second.addPrerequisite("C", "A");
    second.addPrerequisite("C", "B");
    second.addPrerequisite("B", "A");

    // Act + Assert: одинаковый ввод -> одинаковый результат
    assertEquals(first.order(), second.order());
  }

  @Test
  void cycleIsDetected_andDiagnosticPathIsReported() {
    // Arrange: A -> B -> C -> A (цикл)
    PrerequisiteGraph<String> graph = new PrerequisiteGraph<>();
    graph.addPrerequisite("A", "B");
    graph.addPrerequisite("B", "C");
    graph.addPrerequisite("C", "A");

    // Act
    IllegalStateException e = assertThrows(IllegalStateException.class, graph::order);

    // Assert: сообщение показывает путь цикла
    List<String> path = graph.findCycle().orElseThrow();
    assertEquals(path.get(0), path.get(path.size() - 1), "путь замкнут");
    assertTrue(path.size() >= 2);
    assertTrue(path.containsAll(List.of("A", "B", "C")));
    assertTrue(e.getMessage().contains("cycle"));
  }

  @Test
  void selfLoop_isAlsoACycle() {
    // Arrange
    PrerequisiteGraph<String> graph = new PrerequisiteGraph<>();
    graph.addPrerequisite("A", "A");

    // Act + Assert
    assertEquals(List.of("A", "A"), graph.findCycle().orElseThrow());
    assertThrows(IllegalStateException.class, graph::order);
  }

  @Test
  void acyclicGraph_hasNoCyclePath() {
    // Arrange
    PrerequisiteGraph<String> graph = new PrerequisiteGraph<>();
    graph.addPrerequisite("Java", "Base");

    // Act + Assert
    assertTrue(graph.findCycle().isEmpty());
  }

  @Test
  void missingVertex_isNotContained_untilRegistered() {
    // Arrange
    PrerequisiteGraph<String> graph = new PrerequisiteGraph<>();
    graph.addPrerequisite("Java", "Base");

    // Assert: незарегистрированная вершина отсутствует
    assertFalse(graph.contains("Kotlin"));
    assertTrue(graph.contains("Java"));
    assertEquals(2, graph.vertexCount());

    // Act: addPrerequisite сам регистрирует несуществующие вершины
    graph.addPrerequisite("Kotlin", "Java");

    // Assert
    assertTrue(graph.contains("Kotlin"));
    assertEquals(3, graph.vertexCount());
  }

  @Test
  void order_returnsEveryRegisteredVertex() {
    // Arrange
    PrerequisiteGraph<String> graph = new PrerequisiteGraph<>();
    graph.addPrerequisite("Java", "Base");
    graph.register("SQL");

    // Act
    List<String> order = graph.order();

    // Assert: даже независимая вершина не теряется
    assertEquals(3, order.size());
    assertTrue(order.containsAll(List.of("Java", "Base", "SQL")));
  }
}
```

## pom.xml (все задания)

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>coursehub-collections</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <!-- общая модель: src/Course.java и value-объекты, задачи берут их через import src.* -->
        <sourceDirectory>${project.basedir}/src</sourceDirectory>
        <!-- код задания лежит рядом с тестом: task1/... и т.д. -->
        <testSourceDirectory>${project.basedir}/${task.dir}</testSourceDirectory>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <configuration>
                    <!-- тест не должен зависеть от порядка запуска -->
                    <runOrder>random</runOrder>
                </configuration>
            </plugin>
        </plugins>
    </build>

    <profiles>
        <!-- mvn test -> task1, mvn -Ptask2 test -> task2, ... -->
        <profile>
            <id>task1</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <task.dir>task1</task.dir>
            </properties>
        </profile>
        <profile>
            <id>task2</id>
            <properties><task.dir>task2</task.dir></properties>
        </profile>
        <profile>
            <id>task3</id>
            <properties><task.dir>task3</task.dir></properties>
        </profile>
        <profile>
            <id>task4</id>
            <properties><task.dir>task4</task.dir></properties>
        </profile>
        <profile>
            <id>task5</id>
            <properties><task.dir>task5</task.dir></properties>
        </profile>
        <profile>
            <id>task6</id>
            <properties><task.dir>task6</task.dir></properties>
        </profile>
        <profile>
            <id>task7</id>
            <properties><task.dir>task7</task.dir></properties>
        </profile>
        <profile>
            <id>task8</id>
            <properties><task.dir>task8</task.dir></properties>
        </profile>
        <profile>
            <id>task9</id>
            <properties><task.dir>task9</task.dir></properties>
        </profile>
        <profile>
            <id>task10</id>
            <properties><task.dir>task10</task.dir></properties>
        </profile>
        <profile>
            <id>task11</id>
            <properties><task.dir>task11</task.dir></properties>
        </profile>
        <profile>
            <id>task12</id>
            <properties><task.dir>task12</task.dir></properties>
        </profile>
        <profile>
            <id>task13</id>
            <properties><task.dir>task13</task.dir></properties>
        </profile>
        <profile>
            <id>task14</id>
            <properties><task.dir>task14</task.dir></properties>
        </profile>
        <profile>
            <id>task15</id>
            <properties><task.dir>task15</task.dir></properties>
        </profile>
    </profiles>
</project>
```
