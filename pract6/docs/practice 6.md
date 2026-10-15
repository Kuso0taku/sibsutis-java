# ПРАКТИЧЕСКАЯ РАБОТА № 6

## Строки, числа и дата-время в Java

| | |
|---|---|
| Университет | ФГБОУ ВО «Сибирский государственный университет телекоммуникаций и информатики» (СибГУТИ) |
| Институт | Институт вычислительной техники (ИВТ) |
| Кафедра | Кафедра вычислительных систем (ВС) |
| Дисциплина | Основы разработки на Java |
| Студент | Устюжанин Денис Константинович |
| Группа | ИС-541 |
| Преподаватель | Нигматулин Руслан Раульевич |
| Город, дата | Новосибирск, 15.10.2026 |

---

## Введение

### Цель работы

Освоить базовые типы из «повседневного» арсенала Java и их подводные камни:
ссылочность и неизменяемость `String`, регулярные выражения с группами,
автоупаковку и `null`, точные вычисления через `BigDecimal`, масштаб и режимы
округления, воспроизводимую случайность, а также новый API времени
(`LocalDate`, `Period`, `Duration`, `Instant`, `ZonedDateTime`,
`DateTimeFormatter`). Завершается работа сквозной задачей цен и расписания
вебинаров CourseHub, где все эти типы применяются совместно.

### Общая предметная модель

Сквозная тема — учебный центр **CourseHub**: коды курсов, названия, цены,
расписание вебинаров, скидка early-bird и возвраты. Общие типы между заданиями не
копируются, а выносятся в пакет `domain` и переиспользуются: например, `Money`
(задание 14) использует задание 15, `CourseTitle` (задание 13) — задание 15.

### Структура проекта и сборка

В отличие от предыдущих работ, исходники разложены не по пакету на задание, а по
темам, как рекомендовано в `tasks.md`:

```
pract6/
├── pom.xml                       # Maven: release 21
├── tasks.md                      # формулировки заданий
├── src/main/java/coursehub/practice6/
│   ├── strings/                  # задания 1–3  (String, regex)
│   ├── numbers/                  # задания 4–7  (Integer, BigDecimal, Random)
│   ├── time/                     # задания 8–12 (LocalDate, Period, Instant, DST)
│   └── domain/                   # задания 13–15 (CourseTitle, Money, pricing)
└── docs/
    ├── practice 6.md             # этот документ
    └── listing.md                # полные листинги
```

Сборка и запуск демонстраций:

```bash
mvn -f pract6/pom.xml clean compile
java -cp pract6/target/classes coursehub.practice6.strings.StringIdentityDemo
```

```xml
<properties>
    <!-- uuuu в DateTimeFormatter, text blocks, pattern matching — всё доступно в 21 -->
    <maven.compiler.release>21</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```

---

# Уровень I. Строки

## Задание 1. `String`: identity и value

### Формулировка

Предсказать `==` и `equals` для литералов, `new String`, конкатенации
compile-time и runtime, а также `intern`. Сформулировать правило сравнения текста,
не опирающееся на string pool.

### Алгоритм решения

1. Сравнить два литерала: `==` и `equals`.
2. Сравнить литерал и `new String`.
3. Показать разницу конкатенации литералов (сворачивается компилятором) и
   конкатенации с переменной (runtime).
4. Вернуть объект в pool через `intern()` и снова сравнить.
5. Оформить правило сравнения текста отдельно от реализации pool.

### Разбор концепций

`==` для ссылочных типов сравнивает **идентичность** (ту же самую ссылку), а
`equals` — **содержимое**. String pool — это кэш литералов и строк, прошедших
`intern()`. Компилятор сворачивает константные выражения (`"JAVA" + "-101"`) в
один литерал, поэтому такая строка попадает в pool. Конкатенация с участием
переменной выполняется во время исполнения и даёт новый объект вне pool.

Правило, не зависящее от pool: **текст сравниваем через `equals` (или
`Objects.equals`), никогда через `==`**. Pool — деталь реализации JVM, и
полагаться на неё в логике нельзя.

### Ключевые листинги

```java
String a = "JAVA-101";
String b = "JAVA-101";
System.out.println("literal == literal: " + (a == b));          // true (pool)
String c = new String("JAVA-101");
System.out.println("literal == new String: " + (a == c));       // false
String d = "JAVA" + "-101";
System.out.println("literal == compile-time concat: " + (a == d)); // true
String prefix = "JAVA";
String e = prefix + "-101";
System.out.println("literal == runtime concat: " + (a == e));   // false
System.out.println("literal == interned: " + (a == e.intern())); // true
```

```java
// правило: текст сравниваем через equals, а не через == (pool — деталь реализации)
public static boolean same(String left, String right) {
  return java.util.Objects.equals(left, right);
}
```

### Результат

```text
literal == literal: true
literal.equals(literal): true
literal == new String: false
literal.equals(new String): true
literal == compile-time concat: true
literal == runtime concat: false
literal == interned: true
```

---

## Задание 2. Сборка строки

### Формулировка

Сформировать отчёт из 10 000 строк через `+` в цикле и через `StringBuilder`.
Сравнить результат и время как учебный эксперимент (не выдавая его за JMH).
Объяснить неизменяемость `String`.

### Алгоритм решения

1. Реализовать сборку через `+` в цикле.
2. Реализовать сборку через `StringBuilder`.
3. Убедиться, что результаты побайтово совпадают.
4. Замерить время обеих реализаций через `System.nanoTime()`.
5. Явно оговорить, что замер учебный.

### Разбор концепций

`String` **неизменяем** (immutable): любой `+` или `concat` создаёт новый объект.
В цикле `report += line` на каждой итерации копируется всё накопленное, суммарная
сложность — O(n²). `StringBuilder` держит изменяемый буфер (`char[]`/`byte[]`) и
дописывает в него за амортизированное O(1), поэтому линейно.

Замер `System.nanoTime()` не является полноценным бенчмарком: нет прогрева JIT,
нет повторов и статистики. Для настоящих измерений нужен JMH. Здесь важна не
конкретная цифра, а порядок величины и объяснение причины.

### Ключевые листинги

```java
public static String withPlus(List<String> lines) {
  String report = "";
  for (String line : lines) {
    report += line + "\n";
  }
  return report;
}

public static String withBuilder(List<String> lines) {
  StringBuilder sb = new StringBuilder();
  for (String line : lines) {
    sb.append(line).append('\n');
  }
  return sb.toString();
}
```

### Результат

`same result: true`; время `StringBuilder` заметно меньше, чем у `+`, причём
разница растёт с числом строк.

---

## Задание 3. Регулярное выражение с группами

### Формулировка

Проверить код курса вида `JAVA-101`, извлечь prefix и number, запретить частичное
совпадение. Подготовить таблицу валидных и невалидных значений, включая Unicode и
пробелы.

### Алгоритм решения

1. Задать шаблон с двумя группами: `([A-Z]+)` и `(\d+)`.
2. Обязать полное совпадение через `\A ... \z` (или `matches()`).
3. Распарсить группы в `CourseCode`.
4. Прогнать таблицу значений, включая пробелы, строчные буквы и кириллицу.

### Разбор концепций

Группы `(...)` позволяют извлечь части совпадения через `Matcher.group(n)`.
Якоря `\A` и `\z` (в отличие от `^`/`$`, которые могут совпасть на границе строк)
требуют полного совпадения всей строки — так запрещается частичное совпадение
вида `JAVA-101x`. Класс `[A-Z]` намеренно не включает кириллицу и строчные буквы,
поэтому `ЯВА-101` и `java-101` отклоняются.

### Ключевые листинги

```java
private static final Pattern FORMAT = Pattern.compile("\\A([A-Z]+)-(\\d+)\\z");

public static Optional<CourseCode> parse(String raw) {
  if (raw == null) {
    return Optional.empty();
  }
  Matcher matcher = FORMAT.matcher(raw);
  if (!matcher.matches()) {
    return Optional.empty();
  }
  return Optional.of(new CourseCode(matcher.group(1), Integer.parseInt(matcher.group(2))));
}
```

### Результат

| Значение | Результат |
|---|---|
| `JAVA-101` | valid (JAVA, 101) |
| `GO-7` | valid (GO, 7) |
| `JAVA-101x` | invalid (частичное совпадение) |
| `xJAVA-101` | invalid |
| ` JAVA-101` | invalid (ведущий пробел) |
| `JAVA-101 ` | invalid (хвостовой пробел) |
| `java-101` | invalid (строчные) |
| `ЯВА-101` | invalid (кириллица) |
| `JAVA 101` | invalid (пробел вместо дефиса) |
| `JAVA-` | invalid (нет номера) |

---

# Уровень II. Числа

## Задание 4. Autoboxing и `null`

### Формулировка

Воспроизвести NPE при unboxing `Integer`, сравнить `Integer` через `==` на разных
значениях и исправить код через явный контракт. Не полагаться на кэш
wrapper-объектов.

### Алгоритм решения

1. Показать NPE: `Integer value = null; value + 1`.
2. Сравнить `Integer` через `==` для 100 (в кэше) и 1000 (вне кэша).
3. Ввести явный контракт: `null` заменяется на `0`.
4. Сравнение значений — через `Objects.equals`, а не `==`.

### Разбор концепций

Автоупаковка (autoboxing) превращает `int` в `Integer`, авто-распаковка
(unboxing) — обратно. Распаковка `null` бросает `NullPointerException`, причём
место падения не всегда очевидно. Java кэширует `Integer` в диапазоне
`[-128..127]`, поэтому `==` там случайно даёт `true`, а вне диапазона — `false`.
Опираться на этот кэш нельзя. Правильный контракт: явно решить, что значит `null`
(ошибка или `0`), и сравнивать по значению через `equals`.

### Ключевые листинги

```java
public static int plusOneUnsafe(Integer value) {
  return value + 1;                 // NPE при value == null
}

public static int plusOne(Integer value) {
  return value == null ? 0 : value + 1;
}

public static boolean sameByValue(Integer left, Integer right) {
  return java.util.Objects.equals(left, right);
}
```

### Результат

```text
NPE: unboxing null
safe plusOne(null): 0
100 == 100 (ref): true       <- кэш wrapper-объектов
1000 == 1000 (ref): false    <- разные объекты
1000 equals 1000: true
```

---

## Задание 5. `BigDecimal` без сюрприза double

### Формулировка

Сравнить `new BigDecimal(0.1)`, `BigDecimal.valueOf(0.1)` и строковый конструктор.
Рассчитать стоимость трёх курсов и объяснить разницу представлений.

### Алгоритм решения

1. Построить три `BigDecimal` из 0.1 тремя способами.
2. Сравнить их через `equals` и по масштабу.
3. Сложить стоимость трёх курсов без `double`.

### Разбор концепций

`double` хранит двоичную дробь, а `0.1` в двоичной системе бесконечна, поэтому
`new BigDecimal(0.1)` берёт точное машинное значение
`0.1000000000000000055511151231257827...`. `BigDecimal.valueOf(double)` использует
`Double.toString(0.1)` и даёт «человеческие» `0.1`. Строковый конструктор читает
десятичную запись как есть. Для денег применяем строковый конструктор или
`valueOf`, никогда — `new BigDecimal(double)`.

### Ключевые листинги

```java
BigDecimal fromDouble = new BigDecimal(0.1);   // 0.1000000000000000055511...
BigDecimal valueOf = BigDecimal.valueOf(0.1);  // 0.1
BigDecimal fromString = new BigDecimal("0.1"); // 0.1
```

### Результат

```text
new BigDecimal(0.1): 0.1000000000000000055511151231257827021181583404541015625
BigDecimal.valueOf(0.1): 0.1
new BigDecimal("0.1"): 0.1
valueOf.equals(string): true
fromDouble.equals(string): false
three courses total: 5500.49
```

---

## Задание 6. Масштаб и округление

### Формулировка

Распределить скидку между позициями так, чтобы итоговая сумма точно совпала с
ожидаемой. Режим округления передаётся явно; остаток копеек распределяется
детерминированно.

### Алгоритм решения

1. Посчитать долю каждой позиции с явным `RoundingMode`.
2. Сложить доли и получить расхождение с исходной суммой.
3. Раздать остаток копеек по позициям по кругу — детерминированно.

### Разбор концепций

`BigDecimal` состоит из **unscaled value** и **scale**; `setScale` и `divide`
принимают `RoundingMode`. При делении скидки на равные части почти всегда
возникает остаток (например, 100/3 = 33.33 и одна копейка «висит»). Если её
«потерять», сумма долей не совпадёт с исходной — для денег это недопустимо.
Поэтому остаток раздаём маленькими шагами в фиксированном порядке: результат
воспроизводим и сумма точна.

### Ключевые листинги

```java
BigDecimal share = amount
    .multiply(BigDecimal.valueOf(weights[i]))
    .divide(BigDecimal.valueOf(totalWeight), SCALE, mode);

BigDecimal remainder = amount.setScale(SCALE, mode).subtract(distributed);
int index = 0;
while (remainder.signum() != 0) {
  BigDecimal step = remainder.signum() > 0 ? CENT : CENT.negate();
  shares[index] = shares[index].add(step);
  remainder = remainder.subtract(step);
  index = (index + 1) % shares.length;
}
```

### Результат

```text
share: 33.34
share: 33.33
share: 33.33
sum: 100.00
exact: true
by price: 7.50, 2.50
```

---

## Задание 7. `Math` и воспроизводимая случайность

### Формулировка

Сгенерировать учебную выборку через `RandomGenerator` с seed, вычислить границы и
округление. Разделить продакшн-случайность и детерминированный тестовый источник.

### Алгоритм решения

1. Получить продакшн-генератор через `RandomGeneratorFactory`.
2. Получить воспроизводимый генератор через seed.
3. Сгенерировать выборку в диапазоне.
4. Вычислить min, max и среднее с явным округлением.

### Разбор концепций

`RandomGenerator` (Java 17+) — интерфейс, а конкретный алгоритм создаётся фабрикой
`RandomGeneratorFactory`. Разделяя продакшн-источник (без seed) и тестовый (с
seed), мы получаем недетерминированное поведение в бою и повторяемые данные в
тестах. Среднее считаем в `BigDecimal` с явным `RoundingMode`, чтобы избежать
неожиданного целочисленного деления.

### Ключевые листинги

```java
public static RandomGenerator production() {
  return RandomGeneratorFactory.of("L64X128MixRandom").create();
}

public static RandomGenerator seeded(long seed) {
  return RandomGeneratorFactory.of("L64X128MixRandom").create(seed);
}
```

```java
BigDecimal average = BigDecimal.valueOf(sum)
    .divide(BigDecimal.valueOf(values.length), 2, RoundingMode.HALF_UP);
```

### Результат

```text
sample: [37, 62, 25, 13, 31, 40, 33, 41, 93, 63]
same seed -> same sample: true
different seed -> same sample: false
stats: SampleStats[min=13, max=93, average=43.80]
```

---

# Уровень III. Дата и время

## Задание 8. `LocalDate` и календарные правила

### Формулировка

Рассчитать даты начала модулей, последний учебный день и попадание в leap year.
Исходные объекты не меняются; результат операций присваивается.

### Алгоритм решения

1. Хранить дату старта в неизменяемом поле.
2. Построить даты начала модулей шагом в неделях.
3. Найти последний учебный день с учётом выходных.
4. Показать, что исходная дата не изменилась.

### Разбор концепций

`LocalDate` **неизменяем**. Методы `plusWeeks`, `minusDays` возвращают **новый**
объект, поэтому результат обязательно присваивается. Если забыть присвоить, дата
молча не изменится — это частая ошибка. `Year.isLeap(year)` проверяет високосный
год по григорианским правилам.

### Ключевые листинги

```java
LocalDate date = start;
for (int i = 0; i < modules; i++) {
  starts.add(date);
  date = date.plusWeeks(moduleWeeks);   // результат присваиваем
}
```

### Результат

```text
start: 2024-02-05
leap year: true
modules: [2024-02-05, 2024-03-04, 2024-04-01]
last study day: 2024-04-26
start after calculations: 2024-02-05
```

---

## Задание 9. `Period` против `Duration`

### Формулировка

Сравнить «один календарный день» и «24 часа». Подобрать пример, где эти понятия
расходятся после добавления зоны. Объяснить, какой тип подходит сроку подписки, а
какой — timeout.

### Алгоритм решения

1. Взять момент до перехода на летнее время.
2. Прибавить `Period.ofDays(1)` (календарный день).
3. Прибавить `Duration.ofHours(24)` (ровные сутки).
4. Сравнить получившиеся моменты.

### Разбор концепций

`Period` оперирует **календарными** единицами (дни, месяцы, годы) и зависит от
зоны и переходов времени. `Duration` — это **точное** количество времени (часы,
минуты, секунды) между двумя моментами. В день перехода на летнее время
календарный день равен 23 часам, поэтому `Period.ofDays(1)` и `Duration.ofHours(24)`
приводят к разным моментам.

Срок подписки — календарное понятие («месяц доступа»), поэтому подходит `Period`.
Timeout («ждать 30 секунд») — точное время, поэтому подходит `Duration`.

### Ключевые листинги

```java
public ZonedDateTime plusCalendarDays(int days, ZoneId zone) {
  return start.atZone(zone).plus(Period.ofDays(days));
}

public Instant plusExactHours(long hours) {
  return start.plus(Duration.ofHours(hours));
}
```

### Результат

```text
start:             2024-03-10T00:30-05:00[America/New_York]
+1 календарный:    2024-03-11T00:30-04:00[America/New_York]
+24 часа:          2024-03-11T01:30-04:00[America/New_York]
часов в календарном дне: 23
моменты совпали: false
```

---

## Задание 10. Строгий `DateTimeFormatter`

### Формулировка

Парсить даты формата `dd.MM.uuuu`, отклоняя невозможные даты и лишний текст.
Сообщение ошибки содержит поле и исходное значение, но не скрывает cause без
причины.

### Алгоритм решения

1. Собрать `DateTimeFormatter` с шаблоном `dd.MM.uuuu` и `ResolverStyle.STRICT`.
2. Поставить `uuuu` (год-эра), а не `yyyy`.
3. Поймать `DateTimeParseException` и обернуть в своё исключение с полем и
   значением, сохранив cause.

### Разбор концепций

`ResolverStyle.STRICT` не позволяет «умно» превратить `31.02` в `29.02` и
отклоняет невозможные даты. Шаблон `uuuu` означает proleptic year (год-эру); при
`STRICT` комбинация с `yyyy` требует эры и часто ведёт себя неожиданно, поэтому
для строгого разбора используют `uuuu`. Лишний текст (пробел, `г.`) делает строку
невалидной целиком: парсер не «проглатывает» непрочитанный хвост.

Своё исключение хранит поле и исходное значение для понятного сообщения, но
**не теряет** исходный `cause` — при отладке видно настоящую причину.

### Ключевые листинги

```java
private static final DateTimeFormatter FORMAT =
    DateTimeFormatter.ofPattern("dd.MM.uuuu").withResolverStyle(ResolverStyle.STRICT);

public static LocalDate parse(String raw) {
  try {
    return LocalDate.parse(raw, FORMAT);
  } catch (DateTimeParseException e) {
    throw new InvalidDateFieldException("date", raw, e);
  }
}
```

### Результат

| Значение | Результат |
|---|---|
| `01.02.2024` | 2024-02-01 |
| `29.02.2024` | 2024-02-29 |
| `29.02.2023` | invalid (не високосный) |
| `31.02.2024` | invalid (такого дня нет) |
| `01.02.2024 ` | invalid (хвостовой пробел) |
| `01.02.2024 г.` | invalid (лишний текст) |
| `1.2.2024` | invalid (не по шаблону) |

---

## Задание 11. `Instant` и пользовательская зона

### Формулировка

Хранить момент вебинара как `Instant`, отображать в трёх `ZoneId`. Показать, почему
серверная default zone не должна неявно менять бизнес-результат.

### Алгоритм решения

1. Хранить момент как `Instant`.
2. Отображать его в нескольких зонах через `atZone`.
3. Сменить default zone сервера и показать, что `Instant` не изменился.

### Разбор концепций

`Instant` — точка на временной шкале (UTC), не зависящая от зоны. `ZoneId` и
`ZonedDateTime` добавляют календарную интерпретацию. Бизнес-момент (когда будет
вебинар) должен храниться как `Instant`, а зона — использоваться только при
**отображении**. Если полагаться на `ZoneId.systemDefault()`, смена настроек
сервера или переезд пользователя молча изменят вывод — это скрытый дефект.

### Ключевые листинги

```java
public ZonedDateTime displayIn(ZoneId zone) {
  return start.atZone(zone);
}

public List<String> displayAll(ZoneId... zones) {
  List<String> lines = new ArrayList<>();
  for (ZoneId zone : zones) {
    lines.add(zone + " -> " + displayIn(zone).toLocalDateTime());
  }
  return lines;
}
```

### Результат

```text
UTC -> 2024-06-01T15:00
America/New_York -> 2024-06-01T11:00
Asia/Novosibirsk -> 2024-06-01T22:00
Instant unchanged: true
again in UTC:     2024-06-01T15:00
```

---

## Задание 12. DST-кейс

### Формулировка

Найти в выбранной зоне пропущенное или повторённое локальное время. Исследовать
создание `ZonedDateTime`, добавление часа и дня. Зафиксировать политику для
несуществующего времени занятия.

### Алгоритм решения

1. Взять зону с переходом на летнее время.
2. Найти пропущенное локальное время (весенний переход).
3. Найти повторённое локальное время (осенний переход).
4. Задать политику: сдвигать вперёд либо строго отклонять.
5. Показать `plusHours` и `plusDays` через переход.

### Разбор концепций

При переходе на летнее время существует **пропуск** (например, 02:00–03:00
отсутствует), а при переходе на зимнее — **повтор** (01:00–02:00 встречается
дважды). `LocalDateTime.atZone` для пропуска сдвигает время вперёд на величину
скачка, для повтора — берёт более ранний offset. `withLaterOffsetAtOverlap`
позволяет выбрать поздний offset.

Политика для несуществующего времени занятия должна быть **явной**: либо сдвиг
вперёд (как делает `atZone`), либо исключение `InvalidLocalTimeException`. Молча
«додумывать» за пользователя опасно.

### Ключевые листинги

```java
public static ZonedDateTime resolveGapForward(LocalDateTime local, ZoneId zone) {
  return local.atZone(zone);
}

public static ZonedDateTime resolveStrict(LocalDateTime local, ZoneId zone) {
  ZonedDateTime resolved = local.atZone(zone);
  if (!resolved.toLocalDateTime().equals(local)) {
    throw new InvalidLocalTimeException(local, zone);
  }
  return resolved;
}

public static ZonedDateTime laterOffsetAtOverlap(LocalDateTime local, ZoneId zone) {
  return local.atZone(zone).withLaterOffsetAtOverlap();
}
```

### Результат

```text
gap forward: 2024-03-10T03:30-04:00[America/New_York]
strict: local time 2024-03-10T02:30 does not exist in zone America/New_York
overlap earlier: 2024-11-03T01:30-04:00[America/New_York]
overlap later:   2024-11-03T01:30-05:00[America/New_York]
start:        2024-03-10T01:30-05:00[America/New_York]
plus 1 hour:  2024-03-10T03:30-04:00[America/New_York]
plus 1 day:   2024-03-11T01:30-04:00[America/New_York]
```

---

# Уровень IV. Проектирование домена

## Задание 13. Нормализованный поиск

### Формулировка

Создать pipeline нормализации названий: strip, схлопывание пробелов, case folding
по фиксированной locale. Отделить display value от search key и показать конфликт
двух разных исходных строк.

### Алгоритм решения

1. Хранить исходное название (display) и нормализованный ключ (search key).
2. Нормализовать: NFKC → strip → схлопнуть пробелы → lower case по фиксированной
   locale.
3. Индексировать по ключу.
4. Показать, что две разные исходные строки дают один ключ.

### Разбор концепций

**Display value** нужен людям (сохраняем как ввели), **search key** — для поиска
(нормализуем). Нормализация включает Unicode-нормализацию NFKC (приводит
эквивалентные формы к одной), удаление краевых и схлопывание внутренних пробелов,
а также **case folding** с фиксированной locale (`Locale.ROOT`). Фиксированная
locale критична: в турецкой locale строчная `I` ведёт себя иначе, и поиск начал бы
зависеть от машины. Разные исходные строки, дающие один ключ, — это конфликт,
который индекс обязан показывать, а не скрывать.

### Ключевые листинги

```java
public static String searchKeyOf(String raw) {
  return Normalizer.normalize(raw, Normalizer.Form.NFKC)
      .strip()
      .replaceAll("\\s+", " ")
      .toLowerCase(Locale.ROOT);
}
```

### Результат

```text
display1:   '  Java   для   начинающих '
display2:   'java для начинающих'
key1:       'java для начинающих'
key2:       'java для начинающих'
same key:   true
collision: true
```

---

## Задание 14. `Money` и цена курса

### Формулировка

Спроектировать неизменяемый `Money` с amount и currency, запретом смешения валют,
явным округлением и содержательным равенством. Реализовать add, subtract,
percentage и allocation без double.

### Алгоритм решения

1. Хранить `BigDecimal amount` и `Currency currency` в неизменяемых полях.
2. В конструкторе округлять до копеек валюты (`getDefaultFractionDigits`).
3. Запрещать операции над разными валютами.
4. Реализовать `add`, `subtract`, `percentage`, `allocate`.
5. Сделать `equals` по значению (сравнение `compareTo`), согласованный с
   `hashCode`.

### Разбор концепций

**Неизменяемость** гарантирует, что арифметика не меняет исходный объект. Валюта —
часть значения: `100 RUB` и `100 USD` не равны, а попытка их сложить — ошибка
модели. `BigDecimal` не предоставляет «естественного» масштаба для валюты, поэтому
округляем явно до копеек. `equals` для денег — содержательное равенство:
`3000` и `3000.00` равны, поэтому используем `compareTo`, а `hashCode` считаем по
`stripTrailingZeros`, чтобы контракт `equals`/`hashCode` соблюдался.

### Ключевые листинги

```java
public Money add(Money other) {
  requireSameCurrency(other);
  return new Money(amount.add(other.amount), currency);
}

public Money percentage(BigDecimal percent) {
  BigDecimal result = amount
      .multiply(percent)
      .divide(BigDecimal.valueOf(100), currency.getDefaultFractionDigits(), RoundingMode.HALF_UP);
  return new Money(result, currency);
}

@Override
public boolean equals(Object o) {
  if (!(o instanceof Money other)) return false;
  return amount.compareTo(other.amount) == 0 && currency.equals(other.currency);
}
```

### Результат

```text
base:     3000.00 RUB
discount: 300.00 RUB
total:    2700.00 RUB
equals by value: true
parts: 33.34 RUB, 33.33 RUB, 33.33 RUB
sum exact: true
guard: currency mismatch: RUB vs USD
```

---

## Задание 15. Расписание и цена вокруг границ времени

### Формулировка

CourseHub продаёт вебинар в зоне организатора пользователям разных зон, применяет
early-bird до конкретного момента и возвращает деньги пропорционально оставшимся
занятиям. Реализовать модель на `Instant`, `ZonedDateTime`, `LocalDate`,
`Duration`/`Period` и `BigDecimal`. Покрыть DST, leap day, границу скидки, разные
валюты и округление. Защитить выбор каждого типа.

### Алгоритм решения

1. Описать серию вебинаров (`WebinarSeriesPlan`): старт как `Instant`, зона
   организатора, число занятий, длительность как `Duration`.
2. Генерировать моменты занятий шагом `Period.ofWeeks(1)` по календарю
   организатора.
3. Описать продаваемое предложение (`CourseOffering`): план + полная цена.
4. Описать политику раннего бронирования (`EarlyBirdPolicy`): deadline `Instant` и
   процент скидки.
5. Считать цену (`PricingService`) и создавать `Purchase`.
6. Считать возврат (`RefundService`) пропорционально оставшимся занятиям.

### Разбор концепций

**Защита выбора типов.** Момент начала — `Instant`, потому что это точка на шкале
времени, независимая от зоны. Зона организатора хранится отдельно и применяется
для календарных вычислений. Длительность занятия — `Duration` (точное время),
шаг серии — `Period.ofWeeks(1)` (календарный), поэтому местное время занятий не
«съезжает» при переходе на летнее время. Deadline скидки — `Instant`, сравнение
строгое, поэтому на самой границе скидка уже не действует. Деньги — `Money`
(`BigDecimal`), без `double`.

Возврат считается как доля оставшихся занятий: `paid × remaining / total`. При
делении неизбежен остаток, поэтому округление выполняет `Money.percentage` с
заданным режимом. Leap day проверяется отдельно: в 2024 одно из занятий попадает
на 29 февраля.

### Ключевые листинги

```java
public List<Instant> sessionStarts() {
  List<Instant> starts = new ArrayList<>();
  ZonedDateTime session = firstSession.atZone(organizerZone);
  for (int i = 0; i < sessionCount; i++) {
    starts.add(session.toInstant());
    session = session.plus(Period.ofWeeks(1));   // календарный шаг
  }
  return starts;
}

public int remainingSessions(Instant moment) {
  int remaining = 0;
  for (Instant start : sessionStarts()) {
    if (start.isAfter(moment)) remaining++;
  }
  return remaining;
}
```

```java
public Money priceFor(CourseOffering offering, Instant moment) {
  Money full = offering.fullPrice();
  if (!activeAt(moment)) {
    return full;                                 // ровно в deadline — без скидки
  }
  return full.subtract(full.percentage(discountPercent));
}
```

```java
public Money refund(Purchase purchase, Instant moment) {
  int total = purchase.offering().plan().sessionCount();
  int remaining = purchase.offering().plan().remainingSessions(moment);
  if (remaining <= 0) return Money.of("0", purchase.paid().currency());
  BigDecimal percent = BigDecimal.valueOf(remaining)
      .multiply(BigDecimal.valueOf(100))
      .divide(BigDecimal.valueOf(total), 6, RoundingMode.HALF_UP);
  return purchase.paid().percentage(percent);
}
```

### Результат

```text
organizer local times:
  2024-03-06T18:00-05:00[America/New_York]
  2024-03-13T18:00-04:00[America/New_York]   <- после DST время осталось 18:00
  2024-03-20T18:00-04:00[America/New_York]
  2024-03-27T18:00-04:00[America/New_York]
  2024-04-03T18:00-04:00[America/New_York]
viewer in Berlin: 2024-03-07T00:00+01:00[Europe/Berlin]
before deadline: 400.00 USD
at deadline:     500.00 USD
remaining: 3 -> refund 240.00 USD
contains leap day: true
rub price: 36000.00 RUB
currency guard: currency mismatch: USD vs RUB
```

---

## Выводы

В работе разобраны и применены:

- ссылочная семантика `String`, неизменяемость и корректное сравнение текста;
- регулярные выражения с группами и полным совпадением;
- ловушки автоупаковки, `null` и кэша wrapper-объектов;
- точные вычисления `BigDecimal`, масштаб и режимы округления;
- воспроизводимая случайность через `RandomGenerator` с seed;
- неизменяемый API времени и различие календарных и точных величин
  (`Period`/`Duration`);
- строгий разбор дат и явная политика для переходов DST;
- нормализация текста с фиксированной locale;
- неизменяемый денежный тип и сквозная модель цен и возвратов.
