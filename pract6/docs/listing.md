# Практическая работа №6 — листинги

Полные исходные листинги всех заданий. В заголовке указан путь к файлу; в скобках —
задание (задания), в которых файл используется. Общие файлы приведены один раз и
переиспользуются через импорт пакета.

## Содержание

- Сборка
- Задания 1–3. Строки (`strings`)
- Задания 4–7. Числа (`numbers`)
- Задания 8–12. Дата и время (`time`)
- Задания 13–15. Домен (`domain`)

---

## Сборка

### pract6/pom.xml (все задания)

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>coursehub-strings-numbers-time</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <!-- uuuu в DateTimeFormatter, text blocks, pattern matching — всё доступно в 21 -->
        <maven.compiler.release>21</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
</project>
```

---

## Задания 1–3. Строки

### strings/StringIdentityDemo.java (задание 1)

```java
package coursehub.practice6.strings;

// == сравнивает ссылки, equals — содержимое
public class StringIdentityDemo {
  public static void main(String[] args) {
    // литералы: обе ссылки указывают на один объект из string pool
    String a = "JAVA-101";
    String b = "JAVA-101";
    System.out.println("literal == literal: " + (a == b));
    System.out.println("literal.equals(literal): " + a.equals(b));

    // new String всегда создаёт новый объект вне pool
    String c = new String("JAVA-101");
    System.out.println("literal == new String: " + (a == c));
    System.out.println("literal.equals(new String): " + a.equals(c));

    // compile-time конкатенация литералов сворачивается компилятором
    String d = "JAVA" + "-101";
    System.out.println("literal == compile-time concat: " + (a == d));

    // runtime конкатенация даёт новый объект
    String prefix = "JAVA";
    String e = prefix + "-101";
    System.out.println("literal == runtime concat: " + (a == e));

    // intern возвращает каноническую ссылку из pool
    System.out.println("literal == interned: " + (a == e.intern()));
  }
}
```

### strings/TextEquality.java (задание 1)

```java
package coursehub.practice6.strings;

import java.util.Objects;

// правило: текст сравниваем через equals, а не через == (pool — деталь реализации)
public final class TextEquality {
  private TextEquality() {
  }

  // null-safe содержательное сравнение
  public static boolean same(String left, String right) {
    return Objects.equals(left, right);
  }
}
```

### strings/ReportBuilder.java (задание 2)

```java
package coursehub.practice6.strings;

import java.util.List;

// сборка большого текста: + в цикле создаёт новый String на каждой итерации
public final class ReportBuilder {
  private ReportBuilder() {
  }

  // на больших объёмах плохо: каждый + копирует всё накопленное
  public static String withPlus(List<String> lines) {
    String report = "";
    for (String line : lines) {
      report += line + "\n";
    }
    return report;
  }

  // StringBuilder держит изменяемый буфер и не копирует накопленное
  public static String withBuilder(List<String> lines) {
    StringBuilder sb = new StringBuilder();
    for (String line : lines) {
      sb.append(line).append('\n');
    }
    return sb.toString();
  }
}
```

### strings/StringBuildBenchmark.java (задание 2)

```java
package coursehub.practice6.strings;

import java.util.ArrayList;
import java.util.List;

// учебное измерение: это не JMH — без прогрева, повторов и статистики
public class StringBuildBenchmark {
  public static void main(String[] args) {
    List<String> lines = new ArrayList<>();
    for (int i = 1; i <= 10_000; i++) {
      lines.add("row " + i);
    }

    long plusStart = System.nanoTime();
    String byPlus = ReportBuilder.withPlus(lines);
    long plusMs = (System.nanoTime() - plusStart) / 1_000_000;

    long builderStart = System.nanoTime();
    String byBuilder = ReportBuilder.withBuilder(lines);
    long builderMs = (System.nanoTime() - builderStart) / 1_000_000;

    // результат обязан совпасть: разница только в способе сборки
    System.out.println("same result: " + byPlus.equals(byBuilder));
    System.out.println("plus:    " + plusMs + " ms");
    System.out.println("builder: " + builderMs + " ms");
  }
}
```

### strings/CourseCode.java (задание 3)

```java
package coursehub.practice6.strings;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// код курса: префикс и номер как отдельные части, а не одна строка
public record CourseCode(String prefix, int number) {
  // \A ... \z задают полное совпадение: "JAVA-101!" или "xJAVA-101" не пройдут
  private static final Pattern FORMAT = Pattern.compile("\\A([A-Z]+)-(\\d+)\\z");

  // парсинг с извлечением групп 1 (prefix) и 2 (number)
  public static Optional<CourseCode> parse(String raw) {
    if (raw == null) {
      return Optional.empty();
    }
    Matcher matcher = FORMAT.matcher(raw);
    if (!matcher.matches()) {
      return Optional.empty();
    }
    String prefix = matcher.group(1);
    int number = Integer.parseInt(matcher.group(2));
    return Optional.of(new CourseCode(prefix, number));
  }
}
```

### strings/CourseCodeTable.java (задание 3)

```java
package coursehub.practice6.strings;

// таблица валидных и невалидных значений: Unicode, пробелы, частичное совпадение
public class CourseCodeTable {
  public static void main(String[] args) {
    String[] values = {
        "JAVA-101",
        "GO-7",
        "JAVA-101x",   // лишний символ — частичное совпадение запрещено
        "xJAVA-101",   // лишний префикс
        " JAVA-101",   // ведущий пробел
        "JAVA-101 ",   // хвостовой пробел
        "java-101",    // строчные буквы
        "ЯВА-101",     // кириллица: [A-Z] её не принимает
        "JAVA 101",    // пробел вместо дефиса
        "JAVA-",       // нет номера
    };

    for (String value : values) {
      CourseCode.parse(value).ifPresentOrElse(
          code -> System.out.printf("%-12s -> valid   (%s, %d)%n", value, code.prefix(), code.number()),
          () -> System.out.printf("%-12s -> invalid%n", value));
    }
  }
}
```

---

## Задания 4–7. Числа

### numbers/IntegerPolicy.java (задание 4)

```java
package coursehub.practice6.numbers;

// контракт работы с Integer: явно решаем, что делать с null
public final class IntegerPolicy {
  private IntegerPolicy() {
  }

  // unboxing без проверки: null приводит к NullPointerException
  public static int plusOneUnsafe(Integer value) {
    return value + 1;
  }

  // == сравнивает ссылки; для Integer вне кэша (-128..127) это разные объекты
  public static boolean sameByReference(Integer left, Integer right) {
    return left == right;
  }

  // явный контракт: null заменяется на 0
  public static int plusOne(Integer value) {
    return value == null ? 0 : value + 1;
  }

  // содержательное равенство не зависит от кэша wrapper-объектов
  public static boolean sameByValue(Integer left, Integer right) {
    return java.util.Objects.equals(left, right);
  }
}
```

### numbers/AutoboxingDemo.java (задание 4)

```java
package coursehub.practice6.numbers;

// демонстрация: NPE, ловушка == и корректный контракт
public class AutoboxingDemo {
  public static void main(String[] args) {
    // NPE при unboxing null
    try {
      IntegerPolicy.plusOneUnsafe(null);
    } catch (NullPointerException e) {
      System.out.println("NPE: unboxing null");
    }
    System.out.println("safe plusOne(null): " + IntegerPolicy.plusOne(null));

    // кэш -128..127: 100 == 100 истинно, 1000 == 1000 ложно
    Integer cachedA = 100;
    Integer cachedB = 100;
    Integer bigA = 1000;
    Integer bigB = 1000;
    System.out.println("100 == 100 (ref): " + IntegerPolicy.sameByReference(cachedA, cachedB));
    System.out.println("1000 == 1000 (ref): " + IntegerPolicy.sameByReference(bigA, bigB));
    System.out.println("1000 equals 1000: " + IntegerPolicy.sameByValue(bigA, bigB));
  }
}
```

### numbers/DecimalDemo.java (задание 5)

```java
package coursehub.practice6.numbers;

import java.math.BigDecimal;

// три способа получить 0.1 — результат разный
public class DecimalDemo {
  public static void main(String[] args) {
    // double-конструктор берёт точное двоичное значение, а не «0.1»
    BigDecimal fromDouble = new BigDecimal(0.1);
    // valueOf округляет через Double.toString
    BigDecimal valueOf = BigDecimal.valueOf(0.1);
    // строковый конструктор читает десятичную запись как есть
    BigDecimal fromString = new BigDecimal("0.1");

    System.out.println("new BigDecimal(0.1): " + fromDouble);
    System.out.println("BigDecimal.valueOf(0.1): " + valueOf);
    System.out.println("new BigDecimal(\"0.1\"): " + fromString);

    // valueOf и строковый конструктор дают одно и то же значение
    System.out.println("valueOf.equals(string): " + valueOf.equals(fromString));
    // double-конструктор даёт более длинное «хвостатое» число
    System.out.println("fromDouble.equals(string): " + fromDouble.equals(fromString));
    System.out.println("fromDouble.stripTrailingZeros scale: " + fromDouble.stripTrailingZeros().scale());
    System.out.println("fromString.stripTrailingZeros scale: " + fromString.stripTrailingZeros().scale());

    // стоимость трёх курсов: деньги считаем в BigDecimal, не в double
    BigDecimal java = new BigDecimal("3000.00");
    BigDecimal go = BigDecimal.valueOf(1500.50);
    BigDecimal sql = new BigDecimal("999.99");
    BigDecimal total = java.add(go).add(sql);
    System.out.println("three courses total: " + total);
  }
}
```

### numbers/DiscountAllocator.java (задание 6)

```java
package coursehub.practice6.numbers;

import java.math.BigDecimal;
import java.math.RoundingMode;

// делим сумму между позициями так, чтобы сумма долей точно совпала с исходной
public final class DiscountAllocator {
  private static final int SCALE = 2;
  private static final BigDecimal CENT = new BigDecimal("0.01");

  private DiscountAllocator() {
  }

  // режим округления передаётся явно, скрытых правил нет
  public static BigDecimal[] allocate(BigDecimal amount, int[] weights, RoundingMode mode) {
    int totalWeight = 0;
    for (int weight : weights) {
      totalWeight += weight;
    }

    BigDecimal[] shares = new BigDecimal[weights.length];
    BigDecimal distributed = BigDecimal.ZERO.setScale(SCALE);
    for (int i = 0; i < weights.length; i++) {
      BigDecimal share = amount
          .multiply(BigDecimal.valueOf(weights[i]))
          .divide(BigDecimal.valueOf(totalWeight), SCALE, mode);
      shares[i] = share;
      distributed = distributed.add(share);
    }

    // остаток копеек раскидываем по позициям по кругу — порядок детерминирован
    BigDecimal remainder = amount.setScale(SCALE, mode).subtract(distributed);
    int index = 0;
    while (remainder.signum() != 0) {
      BigDecimal step = remainder.signum() > 0 ? CENT : CENT.negate();
      shares[index] = shares[index].add(step);
      remainder = remainder.subtract(step);
      index = (index + 1) % shares.length;
    }
    return shares;
  }
}
```

### numbers/DiscountAllocatorDemo.java (задание 6)

```java
package coursehub.practice6.numbers;

import java.math.BigDecimal;
import java.math.RoundingMode;

// проверяем, что сумма долей ровно равна ожидаемой
public class DiscountAllocatorDemo {
  public static void main(String[] args) {
    BigDecimal discount = new BigDecimal("100.00");
    int[] weights = {1, 1, 1};

    BigDecimal[] shares = DiscountAllocator.allocate(discount, weights, RoundingMode.HALF_UP);

    BigDecimal sum = BigDecimal.ZERO;
    for (BigDecimal share : shares) {
      System.out.println("share: " + share);
      sum = sum.add(share);
    }
    System.out.println("sum: " + sum);
    System.out.println("exact: " + discount.equals(sum));

    // неравные веса: скидка пропорциональна цене позиции
    BigDecimal[] byPrice = DiscountAllocator.allocate(
        new BigDecimal("10.00"), new int[] {3, 1}, RoundingMode.HALF_UP);
    System.out.println("by price: " + byPrice[0] + ", " + byPrice[1]);
  }
}
```

### numbers/RandomSampleService.java (задание 7)

```java
package coursehub.practice6.numbers;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

// отделяем продакшн-случайность от воспроизводимого источника для тестов
public final class RandomSampleService {
  private static final String ALGORITHM = "L64X128MixRandom";

  private RandomSampleService() {
  }

  // продакшн: работаем через интерфейс, не привязываясь к конкретному классу
  public static RandomGenerator production() {
    return RandomGeneratorFactory.of(ALGORITHM).create();
  }

  // тесты: seed делает последовательность воспроизводимой
  public static RandomGenerator seeded(long seed) {
    return RandomGeneratorFactory.of(ALGORITHM).create(seed);
  }

  // выборка в полуинтервале [origin, bound)
  public static int[] sample(RandomGenerator rng, int count, int origin, int bound) {
    int[] values = new int[count];
    for (int i = 0; i < count; i++) {
      values[i] = rng.nextInt(origin, bound);
    }
    return values;
  }
}
```

### numbers/SampleStats.java (задание 7)

```java
package coursehub.practice6.numbers;

import java.math.BigDecimal;
import java.math.RoundingMode;

// границы выборки и округлённое среднее
public record SampleStats(int min, int max, BigDecimal average) {
  public static SampleStats of(int[] values) {
    if (values.length == 0) {
      throw new IllegalArgumentException("empty sample");
    }
    int min = Integer.MAX_VALUE;
    int max = Integer.MIN_VALUE;
    long sum = 0;
    for (int value : values) {
      min = Math.min(min, value);
      max = Math.max(max, value);
      sum += value;
    }
    // среднее округляем явно, а не через неявное деление целых
    BigDecimal average = BigDecimal.valueOf(sum)
        .divide(BigDecimal.valueOf(values.length), 2, RoundingMode.HALF_UP);
    return new SampleStats(min, max, average);
  }
}
```

### numbers/RandomSampleDemo.java (задание 7)

```java
package coursehub.practice6.numbers;

import java.util.Arrays;
import java.util.random.RandomGenerator;

// одинаковый seed -> одинаковая выборка, разный -> разная
public class RandomSampleDemo {
  public static void main(String[] args) {
    RandomGenerator first = RandomSampleService.seeded(42);
    RandomGenerator second = RandomSampleService.seeded(42);
    int[] a = RandomSampleService.sample(first, 10, 1, 100);
    int[] b = RandomSampleService.sample(second, 10, 1, 100);

    System.out.println("sample: " + Arrays.toString(a));
    System.out.println("same seed -> same sample: " + Arrays.equals(a, b));

    RandomGenerator other = RandomSampleService.seeded(43);
    int[] c = RandomSampleService.sample(other, 10, 1, 100);
    System.out.println("different seed -> same sample: " + Arrays.equals(a, c));

    System.out.println("stats: " + SampleStats.of(a));
  }
}
```

---

## Задания 8–12. Дата и время

### time/StudyCalendar.java (задание 8)

```java
package coursehub.practice6.time;

import java.time.LocalDate;
import java.time.Year;

// календарные расчёты: LocalDate неизменяем — результат операции нужно присвоить
public final class StudyCalendar {
  private final LocalDate start;

  public StudyCalendar(LocalDate start) {
    if (start == null) {
      throw new IllegalArgumentException("start must not be null");
    }
    this.start = start;
  }

  public LocalDate start() {
    return start;
  }

  public boolean isLeapYear() {
    return Year.isLeap(start.getYear());
  }

  // даты начала модулей: первый модуль со start, дальше с шагом в неделях
  public java.util.List<LocalDate> moduleStarts(int modules, int moduleWeeks) {
    java.util.List<LocalDate> starts = new java.util.ArrayList<>();
    LocalDate date = start;
    for (int i = 0; i < modules; i++) {
      starts.add(date);
      // plusWeeks не меняет date, поэтому результат присваиваем обратно
      date = date.plusWeeks(moduleWeeks);
    }
    return starts;
  }

  // последний учебный день: конец последней недели, сдвинутый на будний день
  public LocalDate lastStudyDay(int totalWeeks) {
    LocalDate end = start.plusWeeks(totalWeeks).minusDays(1);
    while (end.getDayOfWeek() == java.time.DayOfWeek.SATURDAY
        || end.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
      end = end.minusDays(1);
    }
    return end;
  }
}
```

### time/StudyCalendarDemo.java (задание 8)

```java
package coursehub.practice6.time;

import java.time.LocalDate;

// исходный объект не меняется: start остаётся тем же после всех расчётов
public class StudyCalendarDemo {
  public static void main(String[] args) {
    StudyCalendar calendar = new StudyCalendar(LocalDate.of(2024, 2, 5));

    System.out.println("start: " + calendar.start());
    System.out.println("leap year: " + calendar.isLeapYear());
    System.out.println("modules: " + calendar.moduleStarts(3, 4));
    System.out.println("last study day: " + calendar.lastStudyDay(12));

    // дата не изменилась — LocalDate immutable
    System.out.println("start after calculations: " + calendar.start());
  }
}
```

### time/WebinarSchedule.java (задания 9, 11)

```java
package coursehub.practice6.time;

import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;

// момент вебинара храним как Instant; зона нужна только для календарных расчётов
public final class WebinarSchedule {
  private final Instant start;

  public WebinarSchedule(Instant start) {
    if (start == null) {
      throw new IllegalArgumentException("start must not be null");
    }
    this.start = start;
  }

  public Instant start() {
    return start;
  }

  // Period: «один календарный день» — зависит от зоны и переходов времени
  public ZonedDateTime plusCalendarDays(int days, ZoneId zone) {
    return start.atZone(zone).plus(Period.ofDays(days));
  }

  // Duration: ровные 24 часа от момента — зона не участвует
  public Instant plusExactHours(long hours) {
    return start.plus(java.time.Duration.ofHours(hours));
  }

  // один и тот же Instant, показанный в конкретной зоне
  public ZonedDateTime displayIn(ZoneId zone) {
    return start.atZone(zone);
  }

  // показываем момент сразу в нескольких зонах, не меняя сам момент
  public java.util.List<String> displayAll(ZoneId... zones) {
    java.util.List<String> lines = new java.util.ArrayList<>();
    for (ZoneId zone : zones) {
      lines.add(zone + " -> " + displayIn(zone).toLocalDateTime());
    }
    return lines;
  }
}
```

### time/PeriodVsDurationDemo.java (задание 9)

```java
package coursehub.practice6.time;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

// 10 марта 2024 в США перевели часы вперёд: календарный день короче 24 часов
public class PeriodVsDurationDemo {
  public static void main(String[] args) {
    ZoneId zone = ZoneId.of("America/New_York");
    Instant start = ZonedDateTime.of(2024, 3, 10, 0, 30, 0, 0, zone).toInstant();
    WebinarSchedule schedule = new WebinarSchedule(start);

    ZonedDateTime calendarDay = schedule.plusCalendarDays(1, zone);
    Instant exact24h = schedule.plusExactHours(24);

    System.out.println("start:            " + start.atZone(zone));
    System.out.println("+1 календарный:   " + calendarDay);
    System.out.println("+24 часа:         " + exact24h.atZone(zone));

    // в этот день календарный день равен 23 часам, а не 24
    long hours = Duration.between(start, calendarDay.toInstant()).toHours();
    System.out.println("часов в календарном дне: " + hours);
    System.out.println("моменты совпали: " + calendarDay.toInstant().equals(exact24h));
  }
}
```

### time/DateParser.java (задание 10)

```java
package coursehub.practice6.time;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

// dd.MM.uuuu: строгий разбор — невозможные даты и лишний текст отклоняются
public final class DateParser {
  // ResolverStyle.STRICT не даёт «умно» превратить 31.02 в 29.02
  private static final DateTimeFormatter FORMAT =
      DateTimeFormatter.ofPattern("dd.MM.uuuu").withResolverStyle(ResolverStyle.STRICT);

  private DateParser() {
  }

  public static LocalDate parse(String raw) {
    try {
      return LocalDate.parse(raw, FORMAT);
    } catch (DateTimeParseException e) {
      throw new InvalidDateFieldException("date", raw, e);
    }
  }
}
```

### time/InvalidDateFieldException.java (задание 10)

```java
package coursehub.practice6.time;

// сообщение содержит поле и исходное значение; cause сохраняем, а не прячем
public class InvalidDateFieldException extends RuntimeException {
  private final String field;
  private final String value;

  public InvalidDateFieldException(String field, String value, Throwable cause) {
    super("invalid " + field + ": '" + value + "'", cause);
    this.field = field;
    this.value = value;
  }

  public String field() {
    return field;
  }

  public String value() {
    return value;
  }
}
```

### time/DateParserDemo.java (задание 10)

```java
package coursehub.practice6.time;

// отчётливо видно, какие строки принимаются, а какие нет
public class DateParserDemo {
  public static void main(String[] args) {
    String[] values = {
        "01.02.2024",
        "29.02.2024",   // високосный год — валидно
        "29.02.2023",   // не високосный — невалидно
        "31.02.2024",   // такого дня нет
        "01.02.2024 ",  // лишний пробел в конце
        "01.02.2024 г.",// лишний текст
        "1.2.2024",     // не по шаблону dd.MM.uuuu
    };

    for (String value : values) {
      try {
        System.out.printf("%-15s -> %s%n", value, DateParser.parse(value));
      } catch (InvalidDateFieldException e) {
        System.out.printf("%-15s -> %s (field=%s, cause=%s)%n",
            value, e.getMessage(), e.field(), e.getCause().getClass().getSimpleName());
      }
    }
  }
}
```

### time/ZoneDisplayDemo.java (задание 11)

```java
package coursehub.practice6.time;

import java.time.Instant;
import java.time.ZoneId;
import java.util.TimeZone;

// один момент — разные местные времена; серверная зона не меняет бизнес-результат
public class ZoneDisplayDemo {
  public static void main(String[] args) {
    Instant webinar = Instant.parse("2024-06-01T15:00:00Z");
    WebinarSchedule schedule = new WebinarSchedule(webinar);

    for (String line : schedule.displayAll(
        ZoneId.of("UTC"),
        ZoneId.of("America/New_York"),
        ZoneId.of("Asia/Novosibirsk"))) {
      System.out.println(line);
    }

    // смена default zone сервера не трогает сохранённый Instant
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
    System.out.println("Instant unchanged: " + schedule.start().equals(webinar));
    System.out.println("again in UTC:     " + schedule.displayIn(ZoneId.of("UTC")).toLocalDateTime());
  }
}
```

### time/LocalDateTimeResolver.java (задание 12)

```java
package coursehub.practice6.time;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

// политика для перехода часов: пропуск и повтор местного времени
public final class LocalDateTimeResolver {
  private LocalDateTimeResolver() {
  }

  // в «пропущенном» интервале atZone сдвигает время вперёд на величину скачка
  public static ZonedDateTime resolveGapForward(LocalDateTime local, ZoneId zone) {
    return local.atZone(zone);
  }

  // строгий режим: если после разрешения время не совпало — такого времени нет
  public static ZonedDateTime resolveStrict(LocalDateTime local, ZoneId zone) {
    ZonedDateTime resolved = local.atZone(zone);
    if (!resolved.toLocalDateTime().equals(local)) {
      throw new InvalidLocalTimeException(local, zone);
    }
    return resolved;
  }

  // при «повторе» local.atZone берёт первый offset; этот метод — второй
  public static ZonedDateTime laterOffsetAtOverlap(LocalDateTime local, ZoneId zone) {
    return local.atZone(zone).withLaterOffsetAtOverlap();
  }
}
```

### time/InvalidLocalTimeException.java (задание 12)

```java
package coursehub.practice6.time;

import java.time.LocalDateTime;
import java.time.ZoneId;

// местное время, которого в зоне не существует (пропуск при переходе на лето)
public class InvalidLocalTimeException extends RuntimeException {
  private final LocalDateTime localTime;
  private final ZoneId zone;

  public InvalidLocalTimeException(LocalDateTime localTime, ZoneId zone) {
    super("local time " + localTime + " does not exist in zone " + zone);
    this.localTime = localTime;
    this.zone = zone;
  }

  public LocalDateTime localTime() {
    return localTime;
  }

  public ZoneId zone() {
    return zone;
  }
}
```

### time/DstResolverDemo.java (задание 12)

```java
package coursehub.practice6.time;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

// пропущенное и повторённое местное время; добавление часа и дня
public class DstResolverDemo {
  public static void main(String[] args) {
    ZoneId zone = ZoneId.of("America/New_York");

    // 10 марта 2024, 02:30 — этого времени в зоне не было
    LocalDateTime missing = LocalDateTime.of(2024, 3, 10, 2, 30);
    System.out.println("gap forward: " + LocalDateTimeResolver.resolveGapForward(missing, zone));
    try {
      LocalDateTimeResolver.resolveStrict(missing, zone);
    } catch (InvalidLocalTimeException e) {
      System.out.println("strict: " + e.getMessage());
    }

    // 3 ноября 2024, 01:30 — это время в зоне встретилось дважды
    LocalDateTime repeated = LocalDateTime.of(2024, 11, 3, 1, 30);
    ZonedDateTime earlier = repeated.atZone(zone);
    ZonedDateTime later = LocalDateTimeResolver.laterOffsetAtOverlap(repeated, zone);
    System.out.println("overlap earlier: " + earlier);
    System.out.println("overlap later:   " + later);

    // добавление часа и дня по-разному проходит через переход
    ZonedDateTime start = ZonedDateTime.of(2024, 3, 10, 1, 30, 0, 0, zone);
    System.out.println("start:        " + start);
    System.out.println("plus 1 hour:  " + start.plusHours(1));
    System.out.println("plus 1 day:   " + start.plusDays(1));
  }
}
```

---

## Задания 13–15. Домен

### domain/CourseTitle.java (задания 13, 15)

```java
package coursehub.practice6.domain;

import java.text.Normalizer;
import java.util.Locale;

// display value и search key — разные вещи: показываем как есть, ищем по ключу
public record CourseTitle(String display, String searchKey) {
  public CourseTitle(String display) {
    this(display, searchKeyOf(display));
  }

  // pipeline нормализации: NFKC, strip, схлопывание пробелов, case folding
  public static String searchKeyOf(String raw) {
    if (raw == null) {
      throw new IllegalArgumentException("title must not be null");
    }
    return Normalizer.normalize(raw, Normalizer.Form.NFKC)
        .strip()
        .replaceAll("\\s+", " ")
        .toLowerCase(Locale.ROOT);
  }
}
```

### domain/TitleSearchIndex.java (задание 13)

```java
package coursehub.practice6.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// поиск по нормализованному ключу: разные исходные строки могут попасть в один ключ
public final class TitleSearchIndex {
  private final Map<String, List<CourseTitle>> byKey = new HashMap<>();

  public void add(CourseTitle title) {
    byKey.computeIfAbsent(title.searchKey(), key -> new ArrayList<>()).add(title);
  }

  public List<CourseTitle> find(String query) {
    return byKey.getOrDefault(CourseTitle.searchKeyOf(query), List.of());
  }

  // конфликт: в одном ключе оказалось больше одной исходной строки
  public boolean hasCollision(String query) {
    return find(query).size() > 1;
  }
}
```

### domain/TitleNormalizationDemo.java (задание 13)

```java
package coursehub.practice6.domain;

// две разные исходные строки дают один search key
public class TitleNormalizationDemo {
  public static void main(String[] args) {
    CourseTitle first = new CourseTitle("  Java   для   начинающих ");
    CourseTitle second = new CourseTitle("java для начинающих");

    System.out.println("display1:   '" + first.display() + "'");
    System.out.println("display2:   '" + second.display() + "'");
    System.out.println("key1:       '" + first.searchKey() + "'");
    System.out.println("key2:       '" + second.searchKey() + "'");
    System.out.println("same key:   " + first.searchKey().equals(second.searchKey()));

    TitleSearchIndex index = new TitleSearchIndex();
    index.add(first);
    index.add(second);

    // по нормализованному запросу находятся обе строки — это и есть конфликт
    System.out.println("find ' JAVA  для начинающих ': " + index.find(" JAVA  для начинающих "));
    System.out.println("collision: " + index.hasCollision("Java для начинающих"));
  }
}
```

### domain/Money.java (задания 14, 15)

```java
package coursehub.practice6.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

// неизменяемый денежный тип: сумма и валюта, без double
public final class Money {
  private final BigDecimal amount;
  private final Currency currency;

  private Money(BigDecimal amount, Currency currency) {
    if (amount == null || currency == null) {
      throw new IllegalArgumentException("amount and currency must not be null");
    }
    // явное округление до копеек валюты
    this.amount = amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_UP);
    this.currency = currency;
  }

  public static Money of(BigDecimal amount, Currency currency) {
    return new Money(amount, currency);
  }

  public static Money of(String amount, Currency currency) {
    return new Money(new BigDecimal(amount), currency);
  }

  public BigDecimal amount() {
    return amount;
  }

  public Currency currency() {
    return currency;
  }

  public Money add(Money other) {
    requireSameCurrency(other);
    return new Money(amount.add(other.amount), currency);
  }

  public Money subtract(Money other) {
    requireSameCurrency(other);
    return new Money(amount.subtract(other.amount), currency);
  }

  // смешивать валюты нельзя: это ошибка модели, а не повод молча сложить числа
  private void requireSameCurrency(Money other) {
    if (!currency.equals(other.currency)) {
      throw new IllegalArgumentException("currency mismatch: " + currency + " vs " + other.currency);
    }
  }

  // процент от суммы: деление с явным режимом округления
  public Money percentage(BigDecimal percent) {
    BigDecimal result = amount
        .multiply(percent)
        .divide(BigDecimal.valueOf(100), currency.getDefaultFractionDigits(), RoundingMode.HALF_UP);
    return new Money(result, currency);
  }

  // делим сумму между позициями так, чтобы итог совпал точно
  public Money[] allocate(int... weights) {
    int totalWeight = 0;
    for (int weight : weights) {
      totalWeight += weight;
    }
    Money[] parts = new Money[weights.length];
    BigDecimal distributed = BigDecimal.ZERO.setScale(currency.getDefaultFractionDigits());
    for (int i = 0; i < weights.length; i++) {
      BigDecimal share = amount
          .multiply(BigDecimal.valueOf(weights[i]))
          .divide(BigDecimal.valueOf(totalWeight), currency.getDefaultFractionDigits(), RoundingMode.DOWN);
      parts[i] = new Money(share, currency);
      distributed = distributed.add(share);
    }
    BigDecimal remainder = amount.subtract(distributed);
    BigDecimal cent = BigDecimal.ONE.movePointLeft(currency.getDefaultFractionDigits());
    int index = 0;
    while (remainder.signum() != 0) {
      BigDecimal step = remainder.signum() > 0 ? cent : cent.negate();
      parts[index] = new Money(parts[index].amount.add(step), currency);
      remainder = remainder.subtract(step);
      index = (index + 1) % parts.length;
    }
    return parts;
  }

  // содержательное равенство: сумма и валюта, без учёта масштаба записи
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Money other)) {
      return false;
    }
    return amount.compareTo(other.amount) == 0 && currency.equals(other.currency);
  }

  @Override
  public int hashCode() {
    return Objects.hash(amount.stripTrailingZeros(), currency);
  }

  @Override
  public String toString() {
    return amount + " " + currency.getCurrencyCode();
  }
}
```

### domain/MoneyDemo.java (задание 14)

```java
package coursehub.practice6.domain;

import java.math.BigDecimal;
import java.util.Currency;

// деньги: операции без double, защита от смешения валют
public class MoneyDemo {
  public static void main(String[] args) {
    Currency rub = Currency.getInstance("RUB");
    Currency usd = Currency.getInstance("USD");

    Money base = Money.of("3000", rub);
    Money discount = base.percentage(new BigDecimal("10"));
    Money total = base.subtract(discount);
    System.out.println("base:     " + base);
    System.out.println("discount: " + discount);
    System.out.println("total:    " + total);

    // содержательное равенство: 3000 и 3000.00 — одно и то же
    System.out.println("equals by value: " + Money.of("3000.00", rub).equals(Money.of("3000", rub)));

    // распределение без потери копеек
    Money[] parts = Money.of("100.00", rub).allocate(1, 1, 1);
    Money sum = parts[0].add(parts[1]).add(parts[2]);
    System.out.println("parts: " + parts[0] + ", " + parts[1] + ", " + parts[2]);
    System.out.println("sum exact: " + sum.equals(Money.of("100.00", rub)));

    // смешение валют — ошибка
    try {
      base.add(Money.of("10", usd));
    } catch (IllegalArgumentException e) {
      System.out.println("guard: " + e.getMessage());
    }
  }
}
```

### domain/WebinarSeriesPlan.java (задание 15)

```java
package coursehub.practice6.domain;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

// расписание серии вебинаров: моменты — Instant, календарь — в зоне организатора
public record WebinarSeriesPlan(
    CourseTitle title,
    Instant firstSession,
    ZoneId organizerZone,
    int sessionCount,
    Duration sessionDuration) {

  public WebinarSeriesPlan {
    if (title == null || firstSession == null || organizerZone == null || sessionDuration == null) {
      throw new IllegalArgumentException("required fields must not be null");
    }
    if (sessionCount <= 0) {
      throw new IllegalArgumentException("sessionCount must be > 0");
    }
    if (sessionDuration.isZero() || sessionDuration.isNegative()) {
      throw new IllegalArgumentException("sessionDuration must be positive");
    }
  }

  // сессии идут раз в неделю по календарю организатора — переходы DST учтены
  public List<Instant> sessionStarts() {
    List<Instant> starts = new ArrayList<>();
    ZonedDateTime session = firstSession.atZone(organizerZone);
    for (int i = 0; i < sessionCount; i++) {
      starts.add(session.toInstant());
      session = session.plus(Period.ofWeeks(1));
    }
    return starts;
  }

  // сколько занятий ещё предстоит после указанного момента
  public int remainingSessions(Instant moment) {
    int remaining = 0;
    for (Instant start : sessionStarts()) {
      if (start.isAfter(moment)) {
        remaining++;
      }
    }
    return remaining;
  }

  // местное время начала для пользователя в другой зоне
  public ZonedDateTime firstSessionFor(ZoneId zone) {
    return firstSession.atZone(zone);
  }

  public boolean containsLeapDay() {
    for (Instant start : sessionStarts()) {
      LocalDate date = start.atZone(organizerZone).toLocalDate();
      if (date.getMonthValue() == 2 && date.getDayOfMonth() == 29) {
        return true;
      }
    }
    return false;
  }
}
```

### domain/CourseOffering.java (задание 15)

```java
package coursehub.practice6.domain;

// то, что продаём: расписание и полная цена в одной валюте
public record CourseOffering(WebinarSeriesPlan plan, Money fullPrice) {
  public CourseOffering {
    if (plan == null || fullPrice == null) {
      throw new IllegalArgumentException("plan and fullPrice must not be null");
    }
  }
}
```

### domain/EarlyBirdPolicy.java (задание 15)

```java
package coursehub.practice6.domain;

import java.math.BigDecimal;
import java.time.Instant;

// скидка early-bird действует строго до момента deadline
public record EarlyBirdPolicy(Instant deadline, BigDecimal discountPercent) {
  public EarlyBirdPolicy {
    if (deadline == null || discountPercent == null) {
      throw new IllegalArgumentException("deadline and discountPercent must not be null");
    }
    if (discountPercent.signum() < 0) {
      throw new IllegalArgumentException("discountPercent must be >= 0");
    }
  }

  public boolean activeAt(Instant moment) {
    return moment.isBefore(deadline);
  }

  // на границе скидка уже не действует: сравнение строгое
  public Money priceFor(CourseOffering offering, Instant moment) {
    Money full = offering.fullPrice();
    if (!activeAt(moment)) {
      return full;
    }
    return full.subtract(full.percentage(discountPercent));
  }
}
```

### domain/Purchase.java (задание 15)

```java
package coursehub.practice6.domain;

import java.time.Instant;

// факт покупки: что купили, сколько заплатили и когда
public record Purchase(CourseOffering offering, Money paid, Instant purchasedAt) {
  public Purchase {
    if (offering == null || paid == null || purchasedAt == null) {
      throw new IllegalArgumentException("purchase fields must not be null");
    }
  }
}
```

### domain/PricingService.java (задание 15)

```java
package coursehub.practice6.domain;

import java.time.Instant;

// цену определяет политика early-bird на момент покупки
public final class PricingService {
  private final EarlyBirdPolicy policy;

  public PricingService(EarlyBirdPolicy policy) {
    if (policy == null) {
      throw new IllegalArgumentException("policy must not be null");
    }
    this.policy = policy;
  }

  public Purchase purchase(CourseOffering offering, Instant moment) {
    Money price = policy.priceFor(offering, moment);
    return new Purchase(offering, price, moment);
  }
}
```

### domain/RefundService.java (задание 15)

```java
package coursehub.practice6.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

// возврат пропорционален числу оставшихся занятий
public final class RefundService {
  public Money refund(Purchase purchase, Instant moment) {
    WebinarSeriesPlan plan = purchase.offering().plan();
    int total = plan.sessionCount();
    int remaining = plan.remainingSessions(moment);

    if (remaining <= 0) {
      return Money.of("0", purchase.paid().currency());
    }

    // доля оставшихся занятий; итоговое округление делает Money.percentage
    BigDecimal percent = BigDecimal.valueOf(remaining)
        .multiply(BigDecimal.valueOf(100))
        .divide(BigDecimal.valueOf(total), 6, RoundingMode.HALF_UP);
    return purchase.paid().percentage(percent);
  }
}
```

### domain/PricingDemo.java (задание 15)

```java
package coursehub.practice6.domain;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Currency;

// полный сквозной сценарий: зоны, DST, leap day, граница скидки, возврат, валюты
public class PricingDemo {
  public static void main(String[] args) {
    ZoneId organizer = ZoneId.of("America/New_York");
    Currency usd = Currency.getInstance("USD");

    // 5 занятий по средам в 18:00 по Нью-Йорку, старт до перехода на летнее время
    Instant first = ZonedDateTime.of(2024, 3, 6, 18, 0, 0, 0, organizer).toInstant();
    WebinarSeriesPlan plan = new WebinarSeriesPlan(
        new CourseTitle("Java Webinar Series"), first, organizer, 5, Duration.ofHours(2));
    CourseOffering offering = new CourseOffering(plan, Money.of("500.00", usd));

    // DST: местное время занятия не съезжает
    System.out.println("organizer local times:");
    for (Instant start : plan.sessionStarts()) {
      System.out.println("  " + start.atZone(organizer));
    }
    // пользователь в другой зоне видит свой час, но тот же момент
    System.out.println("viewer in Berlin: " + plan.firstSessionFor(ZoneId.of("Europe/Berlin")));

    // граница скидки: ровно в deadline скидка уже не действует
    Instant deadline = first.minus(Duration.ofDays(7));
    EarlyBirdPolicy earlyBird = new EarlyBirdPolicy(deadline, new BigDecimal("20"));
    PricingService pricing = new PricingService(earlyBird);
    Purchase discounted = pricing.purchase(offering, deadline.minusSeconds(1));
    Purchase full = pricing.purchase(offering, deadline);
    System.out.println("before deadline: " + discounted.paid());
    System.out.println("at deadline:     " + full.paid());

    // возврат: после второго занятия впереди ещё три из пяти
    Instant afterSecond = plan.sessionStarts().get(1).plus(Duration.ofHours(3));
    Money refund = new RefundService().refund(discounted, afterSecond);
    System.out.println("remaining: " + plan.remainingSessions(afterSecond) + " -> refund " + refund);

    // високосный день: одно из занятий приходится на 29 февраля
    Instant leap = ZonedDateTime.of(2024, 2, 29, 18, 0, 0, 0, organizer).toInstant();
    WebinarSeriesPlan leapPlan = new WebinarSeriesPlan(
        new CourseTitle("Leap Series"), leap, organizer, 2, Duration.ofHours(1));
    System.out.println("contains leap day: " + leapPlan.containsLeapDay());

    // другие валюты: цена считается в валюте предложения, смешивать нельзя
    Currency rub = Currency.getInstance("RUB");
    CourseOffering rubOffer = new CourseOffering(plan, Money.of("45000", rub));
    System.out.println("rub price: " + pricing.purchase(rubOffer, deadline.minusSeconds(1)).paid());
    try {
      discounted.paid().add(rubOffer.fullPrice());
    } catch (IllegalArgumentException e) {
      System.out.println("currency guard: " + e.getMessage());
    }
  }
}
```
