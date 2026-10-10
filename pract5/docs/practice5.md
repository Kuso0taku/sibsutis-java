# Практика 5. Коллекции и generics

Разбор всех 15 заданий: формулировка, алгоритм решения, ключевые концепты и
важные листинги. Полные исходники собраны в [listing.md](listing.md), краткие
ответы «на словах» — в `taskN/answer.md`.

## Как устроен проект

```
pract5/
  pom.xml          # 15 профилей: mvn -PtaskN test
  src/             # общая модель и переиспользуемые классы (import src.*)
  taskN/           # код и тесты задания N
  docs/            # этот разбор и листинги
```

- Запуск одного задания: `mvn -PtaskN test` (по умолчанию `task1`).
- `src` компилируется всегда, `taskN` подключается как каталог тестовых
  исходников через свойство `task.dir` в профиле.
- Общая модель (`Course`, `CourseId`, `CourseCode`, `Tag`) лежит в `src` и
  переиспользуется через `import src.*` — она не дублируется по заданиям.

## Общая модель

| Класс        | Роль                                             | Где используется            |
|--------------|--------------------------------------------------|-----------------------------|
| `CourseId`   | value object-идентификатор, `Comparable`         | 3, 6, 8, 10, 11, 12, 13     |
| `CourseCode` | value object с нормализацией, `equals/hashCode`  | 1, 2, 5                     |
| `Course`     | сущность курса, `equals/hashCode` только по `id` | 3, 6, 9, 10, 12, 13         |
| `Tag`        | value object-тег с нормализацией                 | 13                          |

---

## Задание 1. `List` и порядок

**Формулировка.** Сохранить последовательность регистраций, вставить и
удалить элемент, найти дубликаты. Сравнить `ArrayList` и `LinkedList` по
чтению по индексу и по обходу; выбор подтвердить измерением без заявлений о
микробенчмарке.

**Алгоритм.**
1. `CourseRegistry` хранит `ArrayList<CourseCode>` — порядок вставки это и
   есть контракт «последовательность регистраций».
2. `insertAt(index, code)` — вставка в середину (для `ArrayList` это сдвиг
   хвоста, O(n)); `remove(code)` — удаление первого вхождения; `at(i)` — O(1).
3. `sequence()` возвращает `List.copyOf(...)`, чтобы наружу не утекла
   изменяемая внутренняя коллекция.
4. `DuplicateFinder.duplicates` ищет дубликаты в один проход: `HashSet seen`
   и `LinkedHashSet duplicates` (порядок первого повтора).

**Ключевой листинг.**

```java
// DuplicateFinder: O(n) по времени, O(n) по памяти
public static List<CourseCode> duplicates(List<CourseCode> codes) {
  Set<CourseCode> seen = new HashSet<>();
  Set<CourseCode> duplicates = new LinkedHashSet<>();
  for (CourseCode code : codes) {
    if (!seen.add(code)) {   // add вернул false -> уже встречался
      duplicates.add(code);
    }
  }
  return List.copyOf(duplicates);
}
```

**Концепты.**
- `List` — упорядоченная коллекция с доступом по индексу; порядок значим.
- `ArrayList` — массив: `get(i)` O(1), `add` в конец амортизированно O(1),
  `add(i, ...)`/`remove(i)` O(n). Хорошая локальность кэша при обходе.
- `LinkedList` — двусвязный список: `get(i)` O(n) (нужно дойти до узла),
  но вставка/удаление по уже найденному узлу O(1). Узлы — отдельные объекты.
- `List.copyOf` — неизменяемый снимок, защита инвариантов.

**Измерение.** `ListPerformanceDemo` наполняет оба списка на 100 000 элементов
и делает 200 000 случайных `get` и обход. Это демонстрация порядка величин, а
не микробенчмарк: нет прогрева JIT, не контролируется GC. Вывод: случайный
доступ у `ArrayList` радикально быстрее, обход — тоже (непрерывная память).

**Тесты** (`task1/CourseRegistryTest.java`): сохранение порядка, вставка в
середину, удаление только первого вхождения, неизменяемость `sequence()`,
порядок дубликатов по первому повтору.

---

## Задание 2. `Set` и уникальность

**Формулировка.** Удалить дубликаты `CourseCode`, сохранив первый порядок
появления. Выбрать реализацию set и объяснить влияние `equals/hashCode`.

**Алгоритм.** Пройти список один раз, складывая элементы в `LinkedHashSet`.
Он совмещает уникальность `HashSet` и порядок вставки `LinkedHashSet`.

**Ключевой листинг.**

```java
public static List<CourseCode> unique(List<CourseCode> codes) {
  Set<CourseCode> set = new LinkedHashSet<>();
  for (CourseCode code : codes) {
    if (code == null) throw new NullPointerException("code must not be null");
    set.add(code);
  }
  return List.copyOf(set);
}
```

**Выбор реализации.**

| Реализация      | Порядок          | add/contains | Когда                      |
|-----------------|------------------|--------------|----------------------------|
| `HashSet`       | не гарантирован  | O(1)         | порядок не важен           |
| `LinkedHashSet` | порядок вставки  | O(1)         | **нужен порядок появления** |
| `TreeSet`       | отсортирован     | O(log n)     | нужен порядок по значению  |

**Концепты: как `Set` определяет дубликат.**
1. Считает `hashCode` элемента и выбирает корзину.
2. В корзине сравнивает через `equals`.

Отсюда: без `equals`/`hashCode` сравнение идет по ссылке и «копии» не
схлопываются (в тесте это класс `PlainCode`); если `hashCode` не согласован с
`equals`, равные объекты попадают в разные корзины. В `CourseCode` оба метода
построены на нормализованном значении, поэтому `"java-101"`, `" JAVA_101 "`,
`"Java 101"` — один элемент.

**Альтернатива «в лоб»** (`uniqueNaive` через `ArrayList.contains`) дает тот же
результат за O(n²) — включена для сравнения сложности.

---

## Задание 3. `Map` и индекс

**Формулировка.** Построить индекс `CourseId -> Course`, обработать повторный
ID без молчаливой перезаписи. Сравнить `get`, `containsKey`, `putIfAbsent`,
`computeIfAbsent`.

**Алгоритм.** `LinkedHashMap<CourseId, Course>` (сохраняет порядок вставки).
Добавление через `putIfAbsent`: если вернулось не `null` — id уже был, это
ошибка. Явная замена вынесена в `replace` (`put`).

**Ключевой листинг.**

```java
public void add(Course course) {
  if (course == null) throw new NullPointerException("course must not be null");
  Course previous = byId.putIfAbsent(course.id(), course);
  if (previous != null) {
    throw new IllegalStateException("duplicate id: " + course.id());
  }
}

public void replace(Course course) {
  byId.put(course.id(), course);       // намерение "заменить" видно в имени
}

// найти или создать одним действием; фабрика зовется только при промахе
public Course findOrAdd(CourseId id, Supplier<Course> factory) {
  return byId.computeIfAbsent(id, key -> factory.get());
}
```

**Концепты: сравнение методов.**

| Метод              | Смысл                          | Нюанс                                   |
|--------------------|--------------------------------|-----------------------------------------|
| `get(k)`           | значение или `null`            | `null` не отличает «нет ключа» от «null-значение» |
| `containsKey(k)`   | есть ли ключ                   | отдельный поиск, обычно затем второй `get` |
| `putIfAbsent(k,v)` | вставить, если ключа нет       | возвращает старое значение или `null`   |
| `computeIfAbsent(k,f)` | вычислить и вставить       | фабрика только при промахе              |

`get` + `containsKey` = два поиска на операцию. `computeIfAbsent` делает
«найти или создать» одним вызовом; тест с `AtomicInteger` считает вызовы
фабрики и доказывает, что она срабатывает один раз.

---

## Задание 4. Очередь ожидания

**Формулировка.** Реализовать FIFO waitlist через `Queue`: добавить,
посмотреть следующего, извлечь, обработать пустую очередь. Объяснить пары
`add/offer`, `remove/poll`, `element/peek`.

**Алгоритм.** `ArrayDeque` как `Queue` (строгий FIFO, O(1) на концах).
Мягкие операции возвращают `Optional` (`next`, `take`), строгие бросают.

**Ключевой листинг.**

```java
private final Queue<Long> queue = new ArrayDeque<>();

public boolean enqueue(long studentId) { return queue.offer(studentId); }
public Long  nextOrNull()               { return queue.peek(); }
public Long  takeOrNull()               { return queue.poll(); }
public void  enqueueOrThrow(long id)    { queue.add(id); }
public long  nextOrThrow()              { return queue.element(); }
public long  takeOrThrow()              { return queue.remove(); }
```

**Концепты: пары методов.**

| Операция           | Мягкий  | Строгий    | Пустая очередь                          |
|--------------------|---------|------------|-----------------------------------------|
| добавить в конец   | `offer` | `add`      | `offer` → `false`, `add` → исключение   |
| посмотреть первого | `peek`  | `element`  | `peek` → `null`, `element` → исключение |
| извлечь первого    | `poll`  | `remove`   | `poll` → `null`, `remove` → исключение  |

`add/remove/element` — стиль `Collection` (ошибка через исключение);
`offer/poll/peek` — стиль `Queue` (сигнал через `false`/`null`). `ArrayDeque`
предпочтительнее `LinkedList`: та же асимптотика, но без узлов-объектов.

---

## Задание 5. Безопасное удаление при обходе

**Формулировка.** Воспроизвести `ConcurrentModificationException` при удалении
из list в enhanced for. Исправить через iterator и через `removeIf`.
Объяснить, почему копирование всей коллекции не всегда лучший default.

**Алгоритм и воспроизведение.** Enhanced for компилируется в обход через
`iterator`, который хранит `expectedModCount`. `list.remove` меняет `modCount`
— на следующем шаге итератор видит расхождение и падает.

**Ключевой листинг.**

```java
// плохо: прямое удаление во время обхода
for (String code : codes) {
  if (code.equals("JAVA101")) codes.remove(code); // ConcurrentModificationException
}

// хорошо: удаляет итератор
Iterator<CourseCode> it = codes.iterator();
while (it.hasNext()) {
  if (it.next().equals(target)) it.remove();
}

// хорошо: без ручного цикла
codes.removeIf(code -> code.equals(target));
```

**Важно:** CME не гарантирован при удалении последнего просмотренного
элемента — цикл может завершиться раньше проверки. Поэтому в демо удаляется
первый элемент: после сдвига итератор делает ещё один `next()` и замечает
изменение.

**Почему копия не всегда лучший default.**
- Память: O(n) копии там, где нужно удалить один элемент.
- Время: O(n) на копирование всегда, даже если удалений нет; `iterator.remove`
  платит только за реальные удаления.
- Ранний выход: нашли одно совпадение и вышли — копия уже построена целиком.
- Живые представления: `subList`, `Map.values()` — копия разрывает связь.

Копия оправдана при массовых удалениях или конкурентном изменении.

---

## Задание 6. Первый generic-тип

**Формулировка.** Реализовать `Result<T>` или `Page<T>` без raw types. Добавить
фабрики успеха/ошибки, инварианты и методы чтения. Показать две конкретизации
и compile-time защиту от смешения.

**Алгоритм.** `Result<T>` — ровно одно из двух состояний (значение/ошибка).
Приватный конструктор, фабрики `ok`/`error`, проверка инварианта. `Page<T>` —
неизменяемая страница с копированием списка.

**Ключевой листинг.**

```java
public final class Result<T> {
  private final T value;        // != null при успехе
  private final String error;   // != null при ошибке

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

  public <R> Result<R> map(Function<? super T, ? extends R> mapper) {
    if (isError()) return error(error);   // ошибка проходит насквозь
    return ok(mapper.apply(value));
  }
}
```

**Концепты.**
- Generic-тип фиксирует `T` при создании; raw types (`Result` без `<...>`)
  отключают проверку типов и возвращают `Object`.
- Две конкретизации: `Result<Course>` и `Result<CourseId>` — разные типы.
- Compile-time защита: `Result<CourseId> x = Result.ok(course)` не
  компилируется (в тесте оставлено комментарием).
- `Page<T>` копирует входной список (`List.copyOf`) — инвариант
  неизменяемости, наружу не утекает изменяемая коллекция.

---

## Задание 7. Generic-метод

**Формулировка.** Написать `firstOrThrow(List<T>)`, `swap(List<T>, int, int)`
и `indexBy(List<T>, Function<T,K>)`. Последний должен явно обрабатывать
конфликт ключей.

**Алгоритм.**
- `firstOrThrow` — явная ошибка вместо `null` на пустом списке.
- `swap` — проверка границ, затем обмен через временную переменную.
- `indexBy` — `LinkedHashMap` + `putIfAbsent`; конфликт ключей бросает
  `IllegalStateException`. Есть перегрузка с `BinaryOperator`-стратегией.

**Ключевой листинг.**

```java
public static <T, K> Map<K, T> indexBy(List<T> items, Function<T, K> keyFn) {
  Map<K, T> index = new LinkedHashMap<>();
  for (T item : items) {
    K key = keyFn.apply(item);
    T previous = index.putIfAbsent(key, item);
    if (previous != null) {
      throw new IllegalStateException(
          "duplicate key " + key + ": " + previous + " and " + item);
    }
  }
  return index;
}

// когда конфликт ожидаем - стратегия задается снаружи
public static <T, K> Map<K, T> indexBy(
    List<T> items, Function<T, K> keyFn, BinaryOperator<T> merge) {
  Map<K, T> index = new LinkedHashMap<>();
  for (T item : items) index.merge(keyFn.apply(item), item, merge);
  return index;
}
```

**Концепты.** Параметры типа метода выводятся из аргументов на месте вызова
(`GenericLists.firstOrThrow(List.of(...))`). Конфликт ключей — это не «ошибка
данных», а ситуация, которую надо либо явно запретить (исключение), либо
явно разрешить стратегией — молчаливая перезапись недопустима.

---

## Задание 8. Ограничение типа

**Формулировка.** Реализовать `max` для `Comparable`-элементов. Корректно
сформулировать bound с учетом подтипов. Проверить на `Integer`, `String` и
собственном value object.

**Алгоритм.** Линейный проход: текущий максимум, сравнение `compareTo`.

**Ключевой листинг.**

```java
public static <T extends Comparable<? super T>> T max(List<? extends T> items) {
  if (items.isEmpty()) throw new NoSuchElementException("items is empty");
  T best = items.get(0);
  for (T item : items) {
    if (item.compareTo(best) > 0) best = item;
  }
  return best;
}
```

**Концепты.** Граница `T extends Comparable<? super T>` шире, чем
`Comparable<T>`, и означает «`compareTo` принимает `T` или его предка».

- `java.sql.Timestamp extends java.util.Date`, а `compareTo` объявлен в
  `Date` — такой тип подходит только с `? super T`.
- Подкласс, унаследовавший `compareTo` от супертипа (`Derived extends Base`,
  `Base implements Comparable<Base>`), тоже проходит только с `? super T`
  (проверено тестом).

---

## Задание 9. PECS на практике

**Формулировка.** Создать методы копирования из producer-списка в
consumer-список. Объяснить, почему нельзя добавить конкретный объект в
`List<? extends Course>` и что разрешено читать из `List<? super Course>`.

**Ключевой листинг.**

```java
public static <T> void copy(List<? extends T> source, List<? super T> target) {
  for (T item : source) {
    target.add(item);
  }
}
```

**Концепты (PECS = Producer Extends, Consumer Super).**
- `List<? extends Course>` — «список какого-то неизвестного подтипа
  `Course`». Читать безопасно (`Course c = list.get(0)`), но `add`
  запрещен: компилятор не знает точный подтип.
- `List<? super Course>` — «список `Course` или его предка». Писать `Course`
  можно, читать — только как `Object`.
- Так `List<Integer>` копируется в `List<Number>`: источник дает подтип,
  приемник принимает надтип.

---

## Задание 10. Контракт ключа

**Формулировка.** Проверить сущности `Course` в `HashSet` и как ключи
`HashMap`. Изменить поле, не участвующее в равенстве, затем намеренно
включить его в hash и воспроизвести проблему.

**Идея.** `Course` держит `equals/hashCode` только на неизменяемом `id`;
`popularity` — изменяемое поле вне равенства. `BadCourse` включает
`popularity` в `hashCode` и ломает поиск.

**Ключевой листинг.**

```java
// Course: ключ стабилен
@Override public boolean equals(Object o) {
  if (this == o) return true;
  if (!(o instanceof Course other)) return false;
  return id.equals(other.id);
}
@Override public int hashCode() { return Objects.hash(id); }

// BadCourse: мутабельное поле в хеше - ломает set/map
@Override public int hashCode() { return Objects.hash(id, popularity); }
```

**Сценарий проблемы.** Кладем `BadCourse(1, 10)` в `HashSet`; меняем
`popularity` на `999` — корзина, в которую объект попал, больше не совпадает
с текущим хешем. `contains` → `false`, `map.get` → `null`, но `size` = 1:
объект физически на месте, просто недостижим.

**Правила.** `equals` и `hashCode` согласованы; поля из `hashCode` неизменяемы,
пока объект в хеш-коллекции.

---

## Задание 11. Выбор по нагрузке

**Формулировка.** Для шести сценариев выбрать интерфейс и реализацию, указать
сложности и цену памяти/порядка.

**Таблица решений.**

| # | Сценарий                    | Выбор                        | Сложность                         |
|---|-----------------------------|------------------------------|-----------------------------------|
| 1 | каталог по ID               | `Map` → `HashMap`            | `get`/`put` O(1)                  |
| 2 | отсортированный leaderboard | `NavigableMap` → `TreeMap`   | `put` O(log n), `firstKey` O(log n)|
| 3 | очередь                     | `Queue` → `ArrayDeque`       | концы O(1)                        |
| 4 | уникальные теги с порядком  | `Set` → `LinkedHashSet`      | `add`/`contains` O(1)             |
| 5 | диапазонный поиск           | `NavigableMap` → `TreeMap`   | `subMap` O(log n + k)             |
| 6 | частый доступ по индексу    | `List` → `ArrayList`         | `get(i)` O(1)                     |

**Ключевой листинг (диапазонный поиск).**

```java
public SortedMap<CourseId, String> range(long from, long to) {
  if (from > to) throw new IllegalArgumentException("from must be <= to");
  return byId.subMap(new CourseId(from), true, new CourseId(to), true);
}
```

Только упорядоченная структура дает `subMap` за O(log n + k); `HashMap`
вынуждает полный перебор. Конкретные реализации сценариев 2 и 5 — в
`Leaderboard` и `RangeIndex`.

---

## Задание 12. Generic repository

**Формулировка.** Спроектировать `Repository<ID,T>` с `save`, `findById`,
`findAll`, `deleteById`. In-memory реализация не отдает наружу изменяемую
внутреннюю коллекцию, не принимает `null`, определяет семантику повторного
`save`.

**Ключевой листинг.**

```java
public interface Repository<ID, T> {
  void save(T entity);              // повторный save = upsert (замена)
  Optional<T> findById(ID id);
  List<T> findAll();
  boolean deleteById(ID id);
}

public class InMemoryRepository<ID, T> implements Repository<ID, T> {
  private final Map<ID, T> store = new LinkedHashMap<>();
  private final Function<T, ID> idFn;

  @Override public void save(T entity) {
    if (entity == null) throw new NullPointerException("entity must not be null");
    ID id = idFn.apply(entity);
    if (id == null) throw new NullPointerException("id must not be null");
    store.put(id, entity);
  }

  @Override public List<T> findAll() {
    return List.copyOf(store.values());   // неизменяемый снимок
  }
}
```

**Решения.**
- id извлекается функцией `idFn` — репозиторий не знает, чем является сущность;
- `findAll` → `List.copyOf`, поздние `save` не «просвечивают» в старый снимок;
- `null` запрещен на входе (ошибка вызывающего, а не данные);
- повторный `save` c тем же id — замена, размер не растет (upsert).

---

## Задание 13. Обратный индекс

**Формулировка.** По курсам и тегам построить `Map<Tag, Set<CourseId>>`;
поддержать добавление, удаление курса и пересечение нескольких тегов. Пустые
set не остаются в индексе. Обосновать реализацию каждой коллекции.

**Ключевой листинг.**

```java
private final Map<Tag, Set<CourseId>> index = new HashMap<>();

public void add(Course course) {
  for (Tag tag : course.tags()) {
    index.computeIfAbsent(tag, k -> new LinkedHashSet<>()).add(course.id());
  }
}

public void remove(CourseId id, Set<Tag> tags) {
  for (Tag tag : tags) {
    Set<CourseId> ids = index.get(tag);
    if (ids == null) continue;
    ids.remove(id);
    if (ids.isEmpty()) index.remove(tag);   // инвариант: нет пустых set
  }
}

public Set<CourseId> coursesWithAll(Set<Tag> tags) {
  Set<CourseId> result = null;
  for (Tag tag : tags) {
    Set<CourseId> ids = index.get(tag);
    if (ids == null) return Set.of();
    if (result == null) result = new HashSet<>(ids);
    else result.retainAll(ids);
  }
  return Set.copyOf(result);
}
```

**Обоснование.** `Map` — ключ тег, доступ O(1); `Set<CourseId>` — id внутри
тега уникальны, `LinkedHashSet` хранит порядок. Прямое `Map<Course,Set<Tag>>`
решало бы обратную задачу, но «курсы по тегу» требовали бы перебора — потому
и нужен инвертированный индекс.

---

## Задание 14. Ограниченный LRU-кэш

**Формулировка.** Реализовать generic `LruCache<K,V>` фиксированной емкости
на стандартных коллекциях. `get` обновляет недавность, превышение удаляет
старейший, `null`-политика явна. Доказать сценариями порядок вытеснения и
стабильность ключей.

**Ключевой листинг.**

```java
public LruCache(int capacity) {
  if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
  this.capacity = capacity;
  this.map = new LinkedHashMap<>(capacity, 0.75f, true) { // accessOrder = true
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
      return size() > LruCache.this.capacity;
    }
  };
}

public V get(K key) {              // get переставляет запись в конец
  if (key == null) throw new NullPointerException("key must not be null");
  return map.get(key);
}
```

**Концепты.** Два свойства `LinkedHashMap`: `accessOrder=true`
(чтение делает запись самой свежей) и `removeEldestEntry` (вытеснение первой,
т.е. самой старой). Обе операции O(1) амортизированно, память O(capacity).

**Доказательство порядком** (тесты): (1) A,B,C в емкости 3, ставим D →
вытеснен A; (2) после `get(A)` старейшим становится B, значит `put(D)`
вытесняет B, а A остается. `null`-ключи и значения запрещены.

---

## Задание 15. Планировщик prerequisites

**Формулировка.** Курсы образуют ориентированный граф зависимостей.
Построить generic-модель графа на map/set, найти допустимый порядок изучения
или цикл, выдать диагностический путь цикла. Результат детерминирован.
Защитить выбор коллекций, сложности и поведение при отсутствующей вершине.

**Структура.** `Map<V, Set<V>> prereqOf` (курс → его prerequisites) и
`Map<V, Set<V>> dependents` (prereq → кому нужен) плюс `List<V> insertionOrder`
для детерминизма.

**Ключевой листинг (алгоритм Кана).**

```java
public List<V> order() {
  Map<V, Integer> indegree = new HashMap<>();
  for (V v : insertionOrder) indegree.put(v, 0);
  for (var e : prereqOf.entrySet()) indegree.put(e.getKey(), e.getValue().size());

  Deque<V> ready = new ArrayDeque<>();
  for (V v : insertionOrder) if (indegree.get(v) == 0) ready.add(v);

  List<V> result = new ArrayList<>();
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
    throw new IllegalStateException("cycle detected: " + findCycle().orElse(List.of()));
  }
  return List.copyOf(result);
}
```

**Цикл.** Three-color DFS возвращает замкнутый путь `[v1, ..., v1]`; этот
путь попадает в сообщение исключения. Путь восстановлен в порядке
«корень → текущая» (стек развернут).

**Концепты и сложности.**
- Соседние множества — `LinkedHashSet`, порядок вставки → детерминизм.
- `order` и `findCycle` — O(V + E).
- Отсутствующая вершина: `addPrerequisite` сам регистрирует несуществующие
  вершины, `contains` отвечает честно, независимые вершины не теряются.

---

## Итог

Практика покрывает основные структуры коллекций (`List`, `Set`, `Map`,
`Queue`), generics (типы, методы, bounds, PECS), контракты `equals/hashCode`,
а также прикладные структуры (индекс, LRU-кэш, граф зависимостей).
Все 15 профилей проходят тесты: `mvn -PtaskN test`.
