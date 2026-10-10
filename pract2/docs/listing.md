# Листинги к практической работе № 2

## Задание 1

### StackTraceDemo.java (задача 1)

```java
public class StackTraceDemo {
  public static void main(String[] args) {
    a();
  }

  static void a() {
    b();
  }

  static void b() {
    c();
  }

  static void c() {
  // int x = 10/0;
  int x = 10;
  }
}
```

## Задание 2

### answer.md (задача 2)

```
# Обработка исключений в Java: типы и обоснование выбора

| №  | Ситуация                              | Тип                                                       | Почему                                                                                                                       |
|----|---------------------------------------|-----------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------|
| 1  | Неверный аргумент метода              | `unchecked`, `IllegalArgumentException`                   | Это ошибка вызывающего кода. Проверяется в рантайме.                                                                        |
| 2  | Отсутствующий файл                    | `checked`, `FileNotFoundException` / `IOException`        | Внешний ресурс может отсутствовать. Вызывающий обязан решить, что делать.                                                   |
| 3  | Нарушение состояния                   | `unchecked`, `IllegalStateException`                      | Объект не в том состоянии для операции. Это ошибка логики.                                                                  |
| 4  | Сетевой отказ                         | `checked`, `IOException`                                  | Внешняя среда, ожидаемая инфраструктурная проблема.                                                                         |
| 5  | Ошибка конфигурации                   | чаще `checked`, кастомная `ConfigurationException`        | Если конфиг читается при старте и может быть неверным — это ожидаемая проблема. Иногда делают `unchecked`, если падаем сразу. |
| 6  | Недостаток памяти                     | `Error`, `OutOfMemoryError`                               | Обычно невозможно восстановиться. Ловить не надо.                                                                           |
| 7  | Malformed record при импорте          | `checked`, `CourseImportException`                        | Ожидаемая ошибка данных. Нужно сообщить номер записи и причину.                                                             |
| 8  | Нет прав доступа к файлу              | `checked`, `AccessDeniedException` / `IOException`        | Внешняя инфраструктурная ошибка.                                                                                            |
| 9  | Таймаут сети                          | `checked`, `TimeoutException` / `IOException`             | Ожидаемый отказ инфраструктуры.                                                                                             |
| 10 | Программная ошибка: `null` там, где нельзя | `unchecked`, `NullPointerException`                  | Это баг, а не нормальная ситуация.                                                                                          |

---

- **Другой результат**: например, `Optional.empty()` или `Result.error(...)`, когда отсутствие — нормальный сценарий, а не исключение.
- **Невозможность восстановления** = `Error`

## Краткие выводы

- **`checked`** — используется для **ожидаемых** проблем, из которых вызывающий код может и должен восстановиться (внешние ресурсы, сеть, данные, конфигурация).
- **`unchecked` (`RuntimeException`)** — используется для **ошибок программиста**: неверные аргументы, нарушение инвариантов, `null` в неподходящем месте.
- **`Error`** — используется для **фатальных** проблем среды исполнения, восстановление обычно невозможно.

**Эмпирическое правило:**
> Если вызывающий код *может и должен* что-то сделать — делай `checked`.  
> Если это *баг в коде* — делай `unchecked`.  
> Если *всё сломалось настолько, что ничего не поделать* — это `Error`.
```

## Задание 3

### EnrollmentRejectedException.java (задача 3)

```java
public class EnrollmentRejectedException extends Exception {
  public enum ReasonCode {
    COURSE_FULL,
    PREREQUISITE_MISSING,
    ALREADY_ENROLLED,
    COURSE_CLOSED
  }

  private final long studentID;
  private final long courseID;
  private final ReasonCode reasonCode;

  public EnrollmentRejectedException(long studentID, long courseID, 
      ReasonCode reasonCode) {
    super("Enrollment rejected: " + reasonCode);
    this.studentID = studentID;
    this.courseID = courseID;
    this.reasonCode = reasonCode;
  }
  
  public EnrollmentRejectedException(long studentID, long courseID,
      ReasonCode reasonCode, Throwable cause) {
    super("Enrollment rejected: " + reasonCode, cause);
    this.studentID = studentID;
    this.courseID = courseID;
    this.reasonCode = reasonCode;
  }

  public long studentID() { return studentID; }
  public long courseID() { return courseID; }
  public ReasonCode reasonCode() { return reasonCode; }

  @Override
  public String toString() {
    return "EnrollmentRejectedException{reasonCode=" + reasonCode + "}";
  }
}
```

## Задание 4

### examples.java (задача 4)

```java
static int example1() {
  try {
    return 1;
  } finally {
    System.out.println("finally");
  }
}
// it works like:
// 1. count return 1;
// 2. runs finally;
// 3. method return 1;


static int example2() {
  try {
    return 1;
  } finally {
    return 2;
  }
}
// returns 2. return 1 will be lost


static int example3() {
  try {
    throw new RuntimeException("A");
  } finally {
    return 42;
  }
}
// throw exception will be lost, method will return 42
```

## Задание 5

### LoggingResource.java (задача 5)

```java
class LoggingResource implements AutoCloseable {
  private final boolean failOnUse;
  private final boolean failOnClose;

  LoggingResource(boolean failOnUse, boolean failOnClose) {
    this.failOnUse = failOnUse;
    this.failOnClose = failOnClose;
    System.out.println("open");
  }

  void use() {
    System.out.println("use");
    if (failOnUse) {
      throw new RuntimeException("use failed");
    }
  }

  @Override
  public void close() {
    System.out.println("close");
    if (failOnClose) {
      throw new RuntimeException("close failed");
    }
  }
}
```

### Main.java (задача 5)

```java
public class Main {
  public static void main(String[] args) {
    try {
      try (LoggingResource r = new LoggingResource(true, true)) {
        r.use();
      }
    } catch (RuntimeException e) {
      System.out.println("primary = " + e.getMessage());
      for (Throwable suppressed : e.getSuppressed()) {
        System.out.println("suppressed = " + suppressed.getMessage());
      }
    }
  }
}
```

## Задание 6

### CourseImportException.java (задача 6)

```java
public class CourseImportException extends Exception {
  private final int recordNumber;

  public CourseImportException(int recordNumber, String message, Throwable cause) {
    super(message, cause);
    this.recordNumber = recordNumber;
  }

  public int recordNumber() {
    return recordNumber;
  }
}
```

### Main.java (задача 6)

```java
public class Main {
  public static void main(String[] args) throws Exception {
    src.Course c = ParseRecord.parseRecord("42", 1);
    System.out.println(c.durationHours());
  }
}
```

### ParseDuration.java (задача 6)

```java
public class ParseDuration {
    public static int parseDuration(String raw) {
        return Integer.parseInt(raw.trim());
    }
}
```

### ParseRecord.java (задача 6)

```java
import src.Course;

public class ParseRecord {
  public static Course parseRecord(String line, int recordNumber) 
    throws CourseImportException {
      try {
        int duration = ParseDuration.parseDuration(line);
        return new Course(recordNumber, "Imported", duration);
      } catch (NumberFormatException e) {
        throw new CourseImportException(
          recordNumber,
          "Invalid duration in record " + recordNumber,
          e
        );
      }
    }
}
```

## Задание 7

### Enroll.java (задача 7)

```java
public class Enroll {
  public static void enroll(long studentID, long courseID) 
      throws EnrollmentRejectedException {
    if (courseID != 1) {
      throw new EnrollmentRejectedException(
          studentID, courseID,
          EnrollmentRejectedException.ReasonCode.COURSE_CLOSED);
    }
  }
}
```

### FindCourse.java (задача 7)

```java
import java.util.Optional;
import src.Course;

public class FindCourse {
  public static Optional<Course> find(long id) {
    if (id == 1) {
      return Optional.of(new Course(1, "Java", 10));
    }
    return Optional.empty();
  }
}
```

### Main.java (задача 7)

```java
public class Main {
  public static void main(String[] args) {
    // Optional
    for (int i : new int[]{1, 99}) {
      FindCourse.find(i).ifPresentOrElse(
          c -> System.out.println("found id=" + c.id()),
          () -> System.out.println("not found"));

    // checked exception
      try {
        Enroll.enroll(100, i);
        System.out.println("enrolled student=100 course=" + i);
      } catch (EnrollmentRejectedException e) {
        System.out.println("rejected: " + e.reasonCode());
      }
    }
  }
}
```

## Задание 8

### CourseTest.java (задача 8)

```java
import org.junit.jupiter.api.Test;
import src.Course;
import static org.junit.jupiter.api.Assertions.*;

class CourseTest {
  @Test
  void completeHours_increasesCompletedHours_whenEnoughRemaining() {
    // Arrange
    Course course = new Course(1, "Java", 10, 2);

    // Act
    course.completeHours(3);

    // Assert 
    assertEquals(5, course.completedHours(),
        "compoleted hours should increase by 3");
  }
}
```

## Задание 9

### DurationBoundaryTest.java (задача 9)

```java
import org.junit.jupiter.api.Test;
import src.Course;
import static org.junit.jupiter.api.Assertions.*;

// Почему одного "обычного" примера мало:
// пример с durationHours = 10 проходит одинаково и при контракте 1..100,
// и при 1..300, и при 1..10. Он не говорит, где граница, поэтому реализация
// может сломаться (например, 100 -> 300) вместе с тестом зеленым.
// Тест на границах фиксирует контракт: MIN - 1 отклоняется, MIN и MAX
// принимаются, MAX + 1 отклоняется.
class DurationBoundaryTest {
  @Test
  void constructor_rejectsDurationBelowMinimum() {
    // Arrange
    int tooSmall = Course.MIN_DURATION_HOURS - 1;

    // Act
    IllegalArgumentException e = assertThrows(
        IllegalArgumentException.class,
        () -> new Course(1, "Java", tooSmall));

    // Assert
    assertEquals("durationHours must be in [1, 100]", e.getMessage());
  }

  @Test
  void constructor_acceptsMinimumDuration() {
    // Arrange
    int minimum = Course.MIN_DURATION_HOURS;

    // Act
    Course course = new Course(1, "Java", minimum);

    // Assert
    assertEquals(1, course.durationHours(),
        "the minimum duration itself is a valid course");
  }

  @Test
  void constructor_acceptsMaximumDuration() {
    // Arrange
    int maximum = Course.MAX_DURATION_HOURS;

    // Act
    Course course = new Course(1, "Java", maximum);

    // Assert
    assertEquals(100, course.durationHours(),
        "the maximum duration itself is a valid course");
  }

  @Test
  void constructor_rejectsDurationAboveMaximum() {
    // Arrange
    int tooBig = Course.MAX_DURATION_HOURS + 1;

    // Act
    IllegalArgumentException e = assertThrows(
        IllegalArgumentException.class,
        () -> new Course(1, "Java", tooBig));

    // Assert
    assertEquals("durationHours must be in [1, 100]", e.getMessage());
  }

  @Test
  void rejectedDuration_doesNotChangeAnyAcceptedOne() {
    // Arrange: валидный курс строится до невалидного, падение не должно его трогать
    Course course = new Course(1, "Java", Course.MAX_DURATION_HOURS);

    // Act
    assertThrows(IllegalArgumentException.class,
        () -> new Course(2, "Kotlin", Course.MAX_DURATION_HOURS + 1));

    // Assert
    assertEquals(100, course.durationHours());
    assertEquals(0, course.completedHours());
  }
}
```

## Задание 10

### CourseCode.java (задача 10)

```java
import java.util.Locale;

// Код курса приходит из разных источников ("java-101", " JAVA_101 ", "Java 101"),
// но в системе он должен быть один. Нормализация убирает пробелы и разделители,
// приводит к верхнему регистру и только потом проверяется:
// порядок важен, иначе "   " превратится в валидный код.
public class CourseCode {
  public static final int MIN_LENGTH = 3;
  public static final int MAX_LENGTH = 10;

  private final String value;

  private CourseCode(String value) {
    this.value = value;
  }

  public static CourseCode parse(String raw) {
    // null - это ошибка программиста, а не плохие данные:
    // для данных есть IllegalArgumentException с причиной
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
      boolean latinLetterOrDigit =
          (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
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

### CourseCodeTest.java (задача 10)

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

// Параметризованный тест: контракт один (нормализация), а данных много.
// Один метод вместо десяти почти одинаковых, а имя теста показывает вход и
// ожидаемый результат - по отчету видно, какой именно код сломался.
class CourseCodeTest {
  @ParameterizedTest(name = "normalize(\"{0}\") -> \"{1}\"")
  @CsvSource({
      "JAVA101,              JAVA101",
      "' java-101 ',         JAVA101",
      "'java_101',           JAVA101",
      "'Java 101',           JAVA101",
      "'  jAvA   bAsIcs ', JAVABASICS"
  })
  void normalizesRawCodeToCanonicalForm(String raw, String expected) {
    // Act
    CourseCode code = CourseCode.parse(raw);

    // Assert
    assertEquals(expected, code.value());
  }

  @ParameterizedTest(name = "parse(\"{0}\") -> IllegalArgumentException: {1}")
  @MethodSource("invalidCodes")
  void rejectsInvalidCode(String raw, String expectedMessage) {
    // Act
    IllegalArgumentException e = assertThrows(
        IllegalArgumentException.class,
        () -> CourseCode.parse(raw));

    // Assert
    assertEquals(expectedMessage, e.getMessage());
  }

  static Stream<Arguments> invalidCodes() {
    return Stream.of(
        // пусто после нормализации: пробелы и разделители не делают код валидным
        Arguments.of("", "code must not be blank"),
        Arguments.of("   ", "code must not be blank"),
        Arguments.of("-_-_", "code must not be blank"),
        // короче минимума
        Arguments.of("ab", "code length must be in [3, 10]"),
        // длиннее максимума
        Arguments.of("ABCDEFGHIJK", "code length must be in [3, 10]"),
        // посторонние символы видны в сообщении, искать причину не нужно
        Arguments.of("JAVA101!", "code must contain only latin letters and digits, got '!'"));
  }

  @Test
  void acceptsBoundaryLengths() {
    // Arrange
    String shortest = "A".repeat(CourseCode.MIN_LENGTH);
    String longest = "B".repeat(CourseCode.MAX_LENGTH);

    // Act + Assert
    assertEquals(shortest, CourseCode.parse(shortest).value());
    assertEquals(longest, CourseCode.parse(longest).value());
  }

  @Test
  void parsedCodesWithSameNormalizedFormAreEqual() {
    // Arrange
    CourseCode fromCsv = CourseCode.parse("java-101");
    CourseCode fromForm = CourseCode.parse(" JAVA_101 ");

    // Act + Assert: разные источники - один и тот же код в системе
    assertEquals(fromCsv, fromForm);
    assertEquals(fromCsv.hashCode(), fromForm.hashCode());
  }

  @Test
  void parseIsIdempotent() {
    // Arrange
    CourseCode once = CourseCode.parse("java-101");

    // Act
    CourseCode twice = CourseCode.parse(once.value());

    // Assert: нормализация нормализованного не должна ломать код
    assertEquals(once, twice);
  }

  @Test
  void nullIsProgrammingErrorNotInvalidData() {
    // Act + Assert: null - баг вызывающего, его нельзя смешивать с плохими данными
    assertThrows(NullPointerException.class, () -> CourseCode.parse(null));
  }
}
```

## Задание 11

### CourseStateAfterFailureTest.java (задача 11)

```java
import org.junit.jupiter.api.Test;
import src.Course;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// assertThrows отдает саму ошибку, поэтому проверяем не только класс,
// но и то, что ошибка объясняет отказ (сообщение, поля причины),
// и главное - что объект остался в прежнем состоянии.
// В lambda всегда одно падающее действие: если их два, тест не скажет,
// какое именно упало, и починить его почти невозможно.
//
//   assertThrows(IllegalStateException.class, () -> {
//     course.completeHours(999);   // падает здесь?
//     enrollment.enroll(1);       // или здесь?
//   });
class CourseStateAfterFailureTest {
  @Test
  void completeHours_rejectsMoreThanRemaining_andKeepsCourseState()
      throws EnrollmentRejectedException {
    // Arrange
    Course course = new Course(1, "Java", 10, 2);

    // Act: одно падающее действие в lambda
    IllegalStateException e = assertThrows(
        IllegalStateException.class,
        () -> course.completeHours(999));

    // Assert: причина отказа понятна из сообщения
    assertEquals("cannot complete 999 h, only 8 h left", e.getMessage());
    // Assert: объект не тронут
    assertEquals(2, course.completedHours(), "completedHours must stay 2");
    assertEquals(8, course.remainingHours(), "remainingHours must stay 8");
    assertFalse(course.isCompleted());
  }

  @Test
  void completeHours_rejectsNonPositiveHours_withoutTouchingState() {
    // Arrange
    Course course = new Course(1, "Java", 10, 5);

    // Act
    IllegalArgumentException e = assertThrows(
        IllegalArgumentException.class,
        () -> course.completeHours(0));

    // Assert: hours <= 0 - это ошибка вызывающего, не отказ бизнес-правила
    assertEquals("hours must be > 0", e.getMessage());
    assertEquals(5, course.completedHours());
  }

  @Test
  void completeHours_succeeds_andThenTheSameOverflowIsRejected()
      throws EnrollmentRejectedException {
    // Arrange: сначала успешная команда, потом отказ на том же объекте
    Course course = new Course(1, "Java", 10, 0);

    // Act
    course.completeHours(4);

    // Assert
    assertEquals(4, course.completedHours());
    assertThrows(IllegalStateException.class, () -> course.completeHours(7));
    assertEquals(4, course.completedHours(),
        "a rejected command must not change hours");
    assertEquals(6, course.remainingHours());
  }

  @Test
  void enroll_rejectsDuplicate_andKeepsRoster() throws EnrollmentRejectedException {
    // Arrange
    Course course = new Course(2, "Kotlin", 20);
    Enrollment enrollment = new Enrollment(course, 2);
    enrollment.enroll(100);

    // Act: одно падающее действие в lambda
    EnrollmentRejectedException e = assertThrows(
        EnrollmentRejectedException.class,
        () -> enrollment.enroll(100));

    // Assert: сообщение и поля причины отказа
    assertEquals("Enrollment rejected: ALREADY_ENROLLED", e.getMessage());
    assertEquals(100, e.studentID());
    assertEquals(2, e.courseID());
    assertEquals(
        EnrollmentRejectedException.ReasonCode.ALREADY_ENROLLED, e.reasonCode());
    // Assert: состояние не изменилось - ни лишней записи, ни свободного места
    assertEquals(List.of(100L), enrollment.enrolledStudents());
    assertEquals(1, enrollment.seatsLeft());
  }

  @Test
  void enroll_rejectsWhenCourseIsFull_andKeepsRoster()
      throws EnrollmentRejectedException {
    // Arrange: одно место осталось, его занимает другой студент
    Course course = new Course(3, "SQL", 20);
    Enrollment enrollment = new Enrollment(course, 1);
    enrollment.enroll(100);

    // Act
    EnrollmentRejectedException e = assertThrows(
        EnrollmentRejectedException.class,
        () -> enrollment.enroll(200));

    // Assert
    assertEquals(EnrollmentRejectedException.ReasonCode.COURSE_FULL, e.reasonCode());
    assertEquals(200, e.studentID(), "the refused student must be reported");
    assertEquals(3, e.courseID());
    // Assert: отказ не занял место
    assertEquals(List.of(100L), enrollment.enrolledStudents());
    assertEquals(0, enrollment.seatsLeft());
  }

  @Test
  void rejectedStudent_isNotInRosterAndRosterKeepsWorking()
      throws EnrollmentRejectedException {
    // Arrange: единственное место занято
    Course course = new Course(4, "Go", 20);
    Enrollment enrollment = new Enrollment(course, 1);
    enrollment.enroll(100);

    // Act
    assertThrows(EnrollmentRejectedException.class, () -> enrollment.enroll(300));

    // Assert: отсутствие тоже проверяем явно, а не "должно быть пусто"
    assertFalse(enrollment.enrolledStudents().contains(300L));
    assertEquals(List.of(100L), enrollment.enrolledStudents());
    assertEquals(0, enrollment.seatsLeft());

    // Act: отказ не сломал объект - тот же отказ повторяется, список прежний
    assertThrows(EnrollmentRejectedException.class, () -> enrollment.enroll(300));

    // Assert
    assertEquals(List.of(100L), enrollment.enrolledStudents());
  }

  @Test
  void rejectedEnrollmentException_doesNotLeakDataInToString() {
    // Arrange
    Course course = new Course(5, "Rust", 20);
    Enrollment enrollment = new Enrollment(course, 0);

    // Act
    EnrollmentRejectedException e = assertThrows(
        EnrollmentRejectedException.class,
        () -> enrollment.enroll(999));

    // Assert: причина видна, персональные данные - нет
    assertEquals(
        "EnrollmentRejectedException{reasonCode=COURSE_FULL}", e.toString());
    assertFalse(e.toString().contains("999"));
  }
}
```

### Enrollment.java (задача 11)

```java
import src.Course;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Зачисление меняет состояние, поэтому отказ здесь опаснее, чем исключение:
// если enroll() бросит исключение ПОСЛЕ добавления в список,
// в курсе останется студент, которого никто не зачислял.
// Поэтому проверка идет до изменения состояния.
public class Enrollment {
  private final Course course;
  private final int capacity;
  private final Set<Long> students = new LinkedHashSet<>();

  public Enrollment(Course course, int capacity) {
    this.course = course;
    this.capacity = capacity;
  }

  public void enroll(long studentId) throws EnrollmentRejectedException {
    if (students.contains(studentId)) {
      throw new EnrollmentRejectedException(
          studentId, course.id(),
          EnrollmentRejectedException.ReasonCode.ALREADY_ENROLLED);
    }
    if (students.size() >= capacity) {
      throw new EnrollmentRejectedException(
          studentId, course.id(),
          EnrollmentRejectedException.ReasonCode.COURSE_FULL);
    }
    students.add(studentId);
  }

  public List<Long> enrolledStudents() {
    return List.copyOf(students);
  }

  public int seatsLeft() {
    return capacity - students.size();
  }
}
```

### EnrollmentRejectedException.java (задача 11)

```java
public class EnrollmentRejectedException extends Exception {
  public enum ReasonCode {
    COURSE_FULL,
    PREREQUISITE_MISSING,
    ALREADY_ENROLLED,
    COURSE_CLOSED
  }

  private final long studentID;
  private final long courseID;
  private final ReasonCode reasonCode;

  public EnrollmentRejectedException(long studentID, long courseID, 
      ReasonCode reasonCode) {
    super("Enrollment rejected: " + reasonCode);
    this.studentID = studentID;
    this.courseID = courseID;
    this.reasonCode = reasonCode;
  }
  
  public EnrollmentRejectedException(long studentID, long courseID,
      ReasonCode reasonCode, Throwable cause) {
    super("Enrollment rejected: " + reasonCode, cause);
    this.studentID = studentID;
    this.courseID = courseID;
    this.reasonCode = reasonCode;
  }

  public long studentID() { return studentID; }
  public long courseID() { return courseID; }
  public ReasonCode reasonCode() { return reasonCode; }

  @Override
  public String toString() {
    return "EnrollmentRejectedException{reasonCode=" + reasonCode + "}";
  }
}
```

## Задание 12

### BrokenCourseTest.java (задача 12)

```java
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// ЧЕТЫРЕ хрупких места. Этот класс НЕ входит в обычный прогон
// (исключен в pom.xml), он нужен, чтобы увидеть отказ.
// Запуск: mvn -Ptask12 test -Dsurefire.excludes=
//
// Никакого @BeforeEach с очисткой: состояние общее, и это проблема №1 и №2.
// Проблему №3 maven маскирует: surefire всегда запускает тесты
// с рабочей директорией в корне проекта. Прогнав этот тест из другой
// директории (IDE, обычный java -cp), он падает с NoSuchFileException.
class BrokenCourseTest {
  // Проблема 1: тест зависит от порядка - он ищет запись, которую
  // должен был сохранить другой тест. Запущенный по одному, он не падает
  // только потому, что запись осталась от предыдущего прогона.
  @Test
  void find_returnsRecordSavedByAnotherTest() {
    CourseRecord found = StaticCourseRegistry.find(1);

    assertNotNull(found, "record was saved by save_persistsRecord");
    assertEquals("Java", found.title());
  }

  // Проблема 2: static state протекает между тестами. Тест проходит,
  // если он первый, и падает, если перед ним отработал любой другой.
  @Test
  void callLog_containsOnlyItsOwnSaves() {
    StaticCourseRegistry.save(new CourseRecord(7, "Go", 16));

    assertEquals(List.of("save 7"), StaticCourseRegistry.callLog());
  }

  // Проблема 3: путь собран из текущей директории. Работает только если
  // процесс запущен из корня репозитория - из task12/ или из IDE упадет.
  @Test
  void catalog_isLoadedFromWorkingDirectory() throws Exception {
    List<CourseRecord> records =
        CourseCatalog.loadCsv(Path.of("task12", "data", "courses.csv"));

    assertEquals(2, records.size());
  }

  // Проблема 4: тест прибит к тексту toString. Любое изменение формата
  // (новое поле, другой порядок) роняет тест, хотя поведение не изменилось.
  @Test
  void record_toStringMatchesSnapshot() {
    CourseRecord record = new CourseRecord(1, "Java", 32);

    assertEquals("CourseRecord[id=1, title=Java, hours=32]", record.toString());
  }

  // сам тест, на который смотрят другие
  @Test
  void save_persistsRecord() {
    StaticCourseRegistry.save(new CourseRecord(1, "Java", 32));
  }
}
```

### CourseCatalog.java (задача 12)

```java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// Путь передается снаружи - тест сам решает, откуда читать данные.
// Это и есть фикс зависимости от текущей директории.
public class CourseCatalog {
  public static List<CourseRecord> loadCsv(Path path) throws IOException {
    List<CourseRecord> records = new ArrayList<>();
    for (String line : Files.readAllLines(path)) {
      if (line.isBlank()) {
        continue;
      }
      String[] parts = line.split(";");
      records.add(new CourseRecord(
          Long.parseLong(parts[0].trim()),
          parts[1].trim(),
          Integer.parseInt(parts[2].trim())));
    }
    return records;
  }
}
```

### CourseRecord.java (задача 12)

```java
public class CourseRecord {
  private final long id;
  private final String title;
  private final int hours;

  public CourseRecord(long id, String title, int hours) {
    this.id = id;
    this.title = title;
    this.hours = hours;
  }

  public long id() {
    return id;
  }

  public String title() {
    return title;
  }

  public int hours() {
    return hours;
  }

  @Override
  public String toString() {
    return "CourseRecord[id=" + id + ", title=" + title + ", hours=" + hours + "]";
  }
}
```

### CourseRepository.java (задача 12)

```java
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Так выглядит "глобальное состояние", на котором ломаются тесты:
// данные живут в static-поле, а не в объекте.
// Тесты вынуждены вызывать clear() в @BeforeEach, потому что иначе видят
// чужие записи, а в случайном порядке всё равно падают.
// CourseRepository - версия без static, она и используется в RobustCourseTest.
public class CourseRepository {
  private final Map<Long, CourseRecord> records = new LinkedHashMap<>();
  private int saveCalls;

  public void save(CourseRecord record) {
    records.put(record.id(), record);
    saveCalls++;
  }

  public CourseRecord find(long id) {
    return records.get(id);
  }

  public List<CourseRecord> findAll() {
    return List.copyOf(records.values());
  }

  public int count() {
    return records.size();
  }

  // для теста на "повторное сохранение того же id" - состояние видно и извне
  public int saveCalls() {
    return saveCalls;
  }
}

class StaticCourseRegistry {
  private static final Map<Long, CourseRecord> RECORDS = new LinkedHashMap<>();
  private static final List<String> CALL_LOG = new ArrayList<>();

  public static void save(CourseRecord record) {
    RECORDS.put(record.id(), record);
    CALL_LOG.add("save " + record.id());
  }

  public static CourseRecord find(long id) {
    return RECORDS.get(id);
  }

  public static int count() {
    return RECORDS.size();
  }

  public static List<String> callLog() {
    return List.copyOf(CALL_LOG);
  }

  public static void clear() {
    RECORDS.clear();
    CALL_LOG.clear();
  }
}
```

### RobustCourseTest.java (задача 12)

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Те же проверки после починки. Ни очистки, ни фикстур, ни полного toString:
// каждый тест можно запустить отдельно и в любом порядке
// (в pom.xml включен surefire runOrder=random).
class RobustCourseTest {
  // Проблемы 1 и 2: состояние принадлежит объекту, а не static-полю,
  // поэтому тесты не видят друг друга и ничего чистить не нужно
  @Test
  void find_returnsRecordSavedInSameRepository() {
    // Arrange
    CourseRepository repository = new CourseRepository();
    repository.save(new CourseRecord(1, "Java", 32));

    // Act
    CourseRecord found = repository.find(1);

    // Assert
    assertNotNull(found);
    assertEquals("Java", found.title());
    assertEquals(1, repository.count());
  }

  @Test
  void savedRecordIsNotVisibleInAnotherRepository() {
    // Arrange
    CourseRepository repository = new CourseRepository();
    repository.save(new CourseRecord(1, "Java", 32));

    // Act
    CourseRepository another = new CourseRepository();

    // Assert: чужое состояние не протекает
    assertNull(another.find(1), "state leaked between tests");
    assertEquals(0, another.count());
  }

  @Test
  void savingSameIdTwiceReplacesRecordWithoutGrowingState() {
    // Arrange
    CourseRepository repository = new CourseRepository();

    // Act
    repository.save(new CourseRecord(1, "Java", 32));
    repository.save(new CourseRecord(1, "Java v2", 40));

    // Assert
    assertEquals(1, repository.count());
    assertEquals(2, repository.saveCalls(), "both saves happened");
    assertEquals(40, repository.find(1).hours());
  }

  @Test
  void save_keepsInsertionOrder() {
    // Arrange
    CourseRepository repository = new CourseRepository();

    // Act
    repository.save(new CourseRecord(1, "Java", 32));
    repository.save(new CourseRecord(2, "Kotlin", 24));

    // Assert
    assertEquals(List.of(1L, 2L),
        repository.findAll().stream().map(CourseRecord::id).toList());
  }

  // Проблема 3: файл создает сам тест во временной директории.
  // Не нужны ни текущая директория, ни файлы в репозитории.
  @Test
  void catalog_loadsRecordsFromGivenPath(@TempDir Path tmp) throws IOException {
    // Arrange
    Path file = tmp.resolve("courses.csv");
    Files.writeString(file, "1;Java;32\n2;Kotlin;24\n");

    // Act
    List<CourseRecord> records = CourseCatalog.loadCsv(file);

    // Assert
    assertEquals(2, records.size());
    assertEquals("Java", records.get(0).title());
    assertEquals(24, records.get(1).hours());
  }

  @Test
  void catalog_ignoresBlankLines(@TempDir Path tmp) throws IOException {
    // Arrange
    Path file = tmp.resolve("courses.csv");
    Files.writeString(file, "1;Java;32\n\n2;Kotlin;24\n");

    // Act
    List<CourseRecord> records = CourseCatalog.loadCsv(file);

    // Assert
    assertEquals(2, records.size());
  }

  // Проблема 4: проверяем значения, а не текст toString.
  // toString - это вывод для человека, его формат не является контрактом.
  @Test
  void record_exposesItsValuesInsteadOfDependingOnToString() {
    // Arrange
    CourseRecord record = new CourseRecord(1, "Java", 32);

    // Act + Assert
    assertEquals(1, record.id());
    assertEquals("Java", record.title());
    assertEquals(32, record.hours());
  }

  @Test
  void record_toStringMentionsTitleAndHours() {
    // Arrange
    CourseRecord record = new CourseRecord(1, "Java", 32);

    // Act
    String text = record.toString();

    // Assert: интересует только смысл, а не точная строка
    assertTrue(text.contains("Java"), "toString should mention the title");
    assertTrue(text.contains("32"), "toString should mention the hours");
  }
}
```

## Задание 13

### EnrollmentDecisionTest.java (задача 13)

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import src.Course;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

// Таблица решений для зачисления (4 условия):
// 1) статус курса: OPEN / CLOSED
// 2) наличие мест: есть / нет (enrolledCount < capacity)
// 3) prerequisite выполнен: true / false
// 4) повторная заявка: false / true
//
// При фиксированном порядке проверок мы не создаём дублирующих тестов,
// а именно проверяем РЕАЛЬНОЕ правило зачисления.
class EnrollmentDecisionTest {
  @ParameterizedTest(name = "{index}: status={0}, hasSeats={1}, hasPrereq={2}, repeat={3} -> {4}")
  @MethodSource("cases")
  void enrollmentDecision(
      Course.Status status,
      boolean hasSeats,
      boolean hasPrereq,
      boolean repeat,
      Outcome expected) {
    // Arrange
    Course course = new Course(1, "Java", 32);
    course.setStatus(status);
    if (hasSeats) {
      course.setCapacity(2);
    } else {
      course.setCapacity(0);
    }
    course.setHasPrerequisite(hasPrereq);
    if (repeat) {
      course.addEnrolled(42);
    }
    EnrollmentService service = new EnrollmentService();
    long target = repeat ? 43 : 42;
    // Act + Assert
    if (expected == Outcome.ENROLL_OK) {
      int before = course.enrolledCount();
      assertDoesNotThrow(() -> service.enroll(course, target));
      assertTrue(course.enrolledStudents().contains(target));
      assertEquals(before + 1, course.enrolledCount());
    } else {
      int before = course.enrolledCount();
      EnrollmentRejectedException e = assertThrows(
          EnrollmentRejectedException.class,
          () -> service.enroll(course, 42));
      assertEquals(expected.reason(), e.reasonCode());
      assertEquals(before, course.enrolledStudents().size(),
          "rejected enrollment must not add student");
    }
  }

  static Stream<Arguments> cases() {
    return Stream.of(
        // OPEN, есть места, prereq есть, не повтор - успех
        Arguments.of(Course.Status.OPEN, true, true, false, Outcome.ENROLL_OK),
        Arguments.of(Course.Status.CLOSED, true, true, false, Outcome.REJECT_CLOSED),
        Arguments.of(Course.Status.OPEN, false, true, false, Outcome.REJECT_FULL),
        Arguments.of(Course.Status.OPEN, true, false, false, Outcome.REJECT_PREREQ),
        Arguments.of(Course.Status.OPEN, true, true, true, Outcome.REJECT_ALREADY)
    );
  }

  // Проверка эквивалентности: при CLOSED любые комбинации ведут к COURSE_CLOSED,
  // при FULL (нет мест) - к COURSE_FULL и т.п. Таблица не растёт на все 16,
  // потому что границы покрыты минимально необходимыми значимыми случаями.
  @Test
  void whenClosed_noMatterOtherFactors_rejectedAsClosed() {
    // CLOSED + нет мест + нет prereq + повторная заявка
    Course course = new Course(1, "Java", 32);
    course.setStatus(Course.Status.CLOSED);
    course.setCapacity(0);
    course.setHasPrerequisite(false);
    course.addEnrolled(5);
    course.addEnrolled(42);

    EnrollmentService service = new EnrollmentService();
    EnrollmentRejectedException e = assertThrows(
        EnrollmentRejectedException.class,
        () -> service.enroll(course, 42));

    assertEquals(EnrollmentRejectedException.ReasonCode.COURSE_CLOSED, e.reasonCode());
    // состояние осталось прежним
    assertEquals(2, course.enrolledCount());
  }

  @Test
  void whenOpenAndFull_rejectedAsFull_evenIfPrereqMissingOrRepeatWouldMatterLater() {
    // есть приоритет у FULL перед PREREQ/ALREADY согласно порядку
    Course course = new Course(1, "Java", 32);
    course.setStatus(Course.Status.OPEN);
    course.setCapacity(0);
    course.setHasPrerequisite(false);
    EnrollmentService service = new EnrollmentService();

    EnrollmentRejectedException e = assertThrows(
        EnrollmentRejectedException.class,
        () -> service.enroll(course, 42));
    assertEquals(EnrollmentRejectedException.ReasonCode.COURSE_FULL, e.reasonCode());
  }

  @Test
  void whenOpenAndHasSeatsAndHasPrereqAndAlreadyEnrolled_rejectedAsAlready() {
    Course course = new Course(1, "Java", 32);
    course.setStatus(Course.Status.OPEN);
    course.setCapacity(5);
    course.setHasPrerequisite(true);
    course.addEnrolled(42);
    EnrollmentService service = new EnrollmentService();

    EnrollmentRejectedException e = assertThrows(
        EnrollmentRejectedException.class,
        () -> service.enroll(course, 42));
    assertEquals(EnrollmentRejectedException.ReasonCode.ALREADY_ENROLLED, e.reasonCode());
  }

  enum Outcome {
    ENROLL_OK(null),
    REJECT_CLOSED(EnrollmentRejectedException.ReasonCode.COURSE_CLOSED),
    REJECT_FULL(EnrollmentRejectedException.ReasonCode.COURSE_FULL),
    REJECT_PREREQ(EnrollmentRejectedException.ReasonCode.PREREQUISITE_MISSING),
    REJECT_ALREADY(EnrollmentRejectedException.ReasonCode.ALREADY_ENROLLED);

    private final EnrollmentRejectedException.ReasonCode reason;

    Outcome(EnrollmentRejectedException.ReasonCode reason) {
      this.reason = reason;
    }

    EnrollmentRejectedException.ReasonCode reason() {
      return reason;
    }
  }
}
```

### EnrollmentRejectedException.java (задача 13)

```java
public class EnrollmentRejectedException extends Exception {
  public enum ReasonCode {
    COURSE_FULL,
    PREREQUISITE_MISSING,
    ALREADY_ENROLLED,
    COURSE_CLOSED
  }

  private final long studentID;
  private final long courseID;
  private final ReasonCode reasonCode;

  public EnrollmentRejectedException(long studentID, long courseID, 
      ReasonCode reasonCode) {
    super("Enrollment rejected: " + reasonCode);
    this.studentID = studentID;
    this.courseID = courseID;
    this.reasonCode = reasonCode;
  }
  
  public EnrollmentRejectedException(long studentID, long courseID,
      ReasonCode reasonCode, Throwable cause) {
    super("Enrollment rejected: " + reasonCode, cause);
    this.studentID = studentID;
    this.courseID = courseID;
    this.reasonCode = reasonCode;
  }

  public long studentID() { return studentID; }
  public long courseID() { return courseID; }
  public ReasonCode reasonCode() { return reasonCode; }

  @Override
  public String toString() {
    return "EnrollmentRejectedException{reasonCode=" + reasonCode + "}";
  }
}
```

### EnrollmentService.java (задача 13)

```java
import src.Course;

import java.util.Set;

public class EnrollmentService {
  public void enroll(Course course, long studentId) throws EnrollmentRejectedException {
    // порядок проверок фиксируем: сначала статус,
    // затем места, затем prerequisite, затем повторная заявка
    if (course.status() != Course.Status.OPEN) {
      throw new EnrollmentRejectedException(
          studentId, course.id(),
          EnrollmentRejectedException.ReasonCode.COURSE_CLOSED);
    }
    if (course.enrolledCount() >= course.capacity()) {
      throw new EnrollmentRejectedException(
          studentId, course.id(),
          EnrollmentRejectedException.ReasonCode.COURSE_FULL);
    }
    if (!course.hasPrerequisite()) {
      throw new EnrollmentRejectedException(
          studentId, course.id(),
          EnrollmentRejectedException.ReasonCode.PREREQUISITE_MISSING);
    }
    // повторная заявка
    if (course.enrolledStudents().contains(studentId)) {
      throw new EnrollmentRejectedException(
          studentId, course.id(),
          EnrollmentRejectedException.ReasonCode.ALREADY_ENROLLED);
    }
    course.addEnrolled(studentId);
  }
}
```

## Задание 14

### CourseImportException.java (задача 14)

```java
public class CourseImportException extends Exception {
  private final int recordNumber;

  public CourseImportException(int recordNumber, String message, Throwable cause) {
    super(message, cause);
    this.recordNumber = recordNumber;
  }

  public int recordNumber() {
    return recordNumber;
  }
}
```

### CourseImporter.java (задача 14)

```java
import src.Course;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

// Импортёр читает записи построчно. Если запись сломана — бросает
// CourseImportException с номером записи и cause. Если закрылся ресурс —
// IOException в close превращается в suppressed (try-with-resources).
// Никакой частичной публикации: данные накапливаются во временном списке,
// публикуем ТОЛЬКО в конце, когда все записи успешно прочитаны.
public class CourseImporter {
  private final CourseStore store;

  public CourseImporter(CourseStore store) {
    this.store = store;
  }

  public int importFrom(Reader reader) throws IOException, CourseImportException {
    if (reader == null) throw new IllegalArgumentException("reader must not be null");
    List<Course> buffer = new ArrayList<>();
    BufferedReader br = new BufferedReader(reader);
    String line;
    int recordNumber = 0;
    try (br) {
      while ((line = br.readLine()) != null) {
        recordNumber++;
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
        try {
          Course c = parseRecord(trimmed, recordNumber);
          buffer.add(c);
        } catch (Exception ex) {
          throw new CourseImportException(recordNumber,
              "Invalid record at line " + recordNumber, ex);
        }
      }
      // публикация только целиком: buffer содержит все успешно прочитанные записи
      store.publish(buffer);
      return buffer.size();
    } catch (CourseImportException e) {
      throw e;
    } catch (IOException ex) {
      throw ex;
    } catch (Throwable ex) {
      throw new CourseImportException(recordNumber,
          "Invalid record at line " + recordNumber, ex);
    }
  }

  private Course parseRecord(String line, int recordNumber) {
    // формат: id;title;hours
    String[] parts = line.split(";", 3);
    if (parts.length != 3) {
      throw new IllegalArgumentException("bad format");
    }
    long id = Long.parseLong(parts[0].trim());
    String title = parts[1].trim();
    int hours = Integer.parseInt(parts[2].trim());
    return new Course(id, title, hours);
  }
}
```

### CourseImporterTest.java (задача 14)

```java
import org.junit.jupiter.api.Test;
import src.Course;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

// Подавать частично — нельзя. Либо всё, либо ничего.
// При двух отказах (основной и close) основной должен быть primary,
// а close — suppressed.
class CourseImporterTest {
  @Test
  void importsAllValidRecordsAndPublishesAtomically() throws Exception {
    // Arrange
    CourseStore store = new CourseStore();
    CourseImporter importer = new CourseImporter(store);
    StringReader reader = new StringReader(
        "1;Java;32\n" +
            "2;Kotlin;24\n" +
            "# comment\n" +
            "\n" +
            "3;SQL;16\n");

    // Act
    int processed = importer.importFrom(reader);

    // Assert
    assertEquals(3, processed);
    assertEquals(3, store.size());
    assertEquals("Java", store.get(1).title());
    assertEquals(24, store.get(2).durationHours());
    assertEquals(16, store.get(3).durationHours());
  }

  @Test
  void malformedRecordThrowsCourseImportExceptionWithRecordNumberAndCause() {
    // Arrange
    CourseStore store = new CourseStore();
    CourseImporter importer = new CourseImporter(store);
    // record 3 malformed (не число hours)
    StringReader reader = new StringReader(
        "1;Java;32\n" +
            "2;Kotlin;24\n" +
            "3;SQL;bad\n");

    // Act
    CourseImportException e = assertThrows(
        CourseImportException.class,
        () -> importer.importFrom(reader));

    // Assert: номер записи сохраняется и причина (cause) тоже
    assertEquals(3, e.recordNumber());
    assertNotNull(e.getCause(), "cause must be preserved");
    // до ошибки ничего не опубликовано
    assertEquals(0, store.size());
  }

  @Test
  void malformedRecordAndCloseFailure_primaryAndSuppressed() {
    // Arrange: 3-я запись сломана (malformedAt=3) + close падает
    String[] lines = {"1;Java;32", "2;Kotlin;24", "3;SQL;bad"};
    FailingReader reader = new FailingReader(lines, 3, true);
    CourseStore store = new CourseStore();
    CourseImporter importer = new CourseImporter(store);

    // Act
    CourseImportException e = assertThrows(
        CourseImportException.class,
        () -> importer.importFrom(reader));

    // Assert: основное исключение - CourseImportException
    assertEquals(3, e.recordNumber());
    // suppressed: IOException от close()
    Throwable[] suppressed = e.getSuppressed();
    assertEquals(1, suppressed.length);
    assertTrue(suppressed[0] instanceof IOException);
    assertEquals("close failed", suppressed[0].getMessage());
    // ресурсы закрыты
    assertTrue(reader.closed());
    // частично не опубликовали
    assertEquals(0, store.size());
  }

  @Test
  void closeFailureOnly_propagatesIOException() {
    // Arrange: все валидные, но close() падает
    String[] lines = {"1;Java;32", "2;Kotlin;24"};
    FailingReader reader = new FailingReader(lines, 100, true);
    CourseStore store = new CourseStore();
    CourseImporter importer = new CourseImporter(store);

    // Act
    IOException e = assertThrows(IOException.class,
        () -> importer.importFrom(reader));

    // Assert
    assertEquals("close failed", e.getMessage());
    assertTrue(reader.closed());
    // успешные записи были в буфере, но перед публикацией произошло
    // исключение при закрытии? В try-with-resources close() вызывается
    // после тела: публикация store.publish(buffer) происходит в теле,
    // поэтому при успешном чтении всех записей publish выполнится
    // до close(). Если close() упадет - исключение из close() будет
    // suppressed, если основного не было. Но тут основного нет -
    // IOException "close failed" - primary. Publish уже прошёл?
    // Проверим: если чтение успешно, buffer заполнен, publish(buffer) вызван.
    assertEquals(2, store.size(), "store must be published before close");
  }

  @Test
  void malformedBeforeAnyPublish_preventsPartialPublication() {
    // Arrange: первая же строка после пустых - битая
    StringReader reader = new StringReader("\n\nbad;record;here\n");
    CourseStore store = new CourseStore();
    CourseImporter importer = new CourseImporter(store);

    // Act
    CourseImportException e = assertThrows(
        CourseImportException.class,
        () -> importer.importFrom(reader));

    // Assert
    assertEquals(3, e.recordNumber()); // считаем все непустые/не-комментарии? строка "bad;record;here" - 3-я непустая
    assertEquals(0, store.size(), "no partial publish");
  }

  @Test
  void preservesOriginalCauseWhenWrapping() {
    // Arrange
    StringReader reader = new StringReader("1;Java;notanumber\n");
    CourseStore store = new CourseStore();
    CourseImporter importer = new CourseImporter(store);

    // Act
    CourseImportException e = assertThrows(
        CourseImportException.class,
        () -> importer.importFrom(reader));

    // Assert
    assertInstanceOf(NumberFormatException.class, e.getCause());
  }

  @Test
  void processedCountMatchesOnlyPublishedOnes() throws Exception {
    // Arrange
    CourseStore store = new CourseStore();
    CourseImporter importer = new CourseImporter(store);
    StringReader reader = new StringReader("1;Java;32\n2;Kotlin;24\n");

    // Act
    int processed = importer.importFrom(reader);

    // Assert: возвращаем количество обработанных (опубликованных) записей
    assertEquals(2, processed);
    assertEquals(processed, store.size());
  }
}
```

### CourseStore.java (задача 14)

```java
import java.util.LinkedHashMap;
import java.util.Map;

import src.Course;

// CourseStore публикует результат только целиком.
// Записи не добавляются частично: либо импорт успешен, либо ничего не изменилось.
public class CourseStore {
  private final Map<Long, Course> courses = new LinkedHashMap<>();

  public void publish(java.util.Collection<Course> imported) {
    if (imported == null) throw new IllegalArgumentException("imported must not be null");
    // атомарная публикация: сначала проверяем, что можно опубликовать,
    // затем заменяем содержимое
    Map<Long, Course> copy = new LinkedHashMap<>(imported.size());
    for (Course c : imported) {
      if (copy.putIfAbsent(c.id(), c) != null) {
        throw new IllegalArgumentException("duplicate id: " + c.id());
      }
    }
    courses.clear();
    courses.putAll(copy);
  }

  public Course get(long id) {
    return courses.get(id);
  }

  public int size() {
    return courses.size();
  }

  public void clear() {
    courses.clear();
  }
}
```

### FailingReader.java (задача 14)

```java
import java.io.IOException;
import java.io.Reader;

public class FailingReader extends Reader {
  private final String[] lines;
  private final int malformedAt;
  private int index;
  private final boolean failOnClose;
  private boolean closed;

  public FailingReader(String[] lines, int malformedAt, boolean failOnClose) {
    this.lines = lines == null ? new String[0] : lines;
    this.malformedAt = malformedAt;
    this.failOnClose = failOnClose;
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    if (index < lines.length) {
      if (malformedAt >= 1 && index == malformedAt - 1) {
        // Содержимое "битой" записи отдаём целиком, чтобы
        // BufferedReader прочитал её как строку №3.
        // Ошибку записи будем симулировать позже при парсинге? Но мы контролируем строку.
        // Просто отдадим строку и выставим флаг: при следующем? Или лучше отдать строку,
        // но в импортере мы уже обработали предыдущие — строка будет прочитана.
        String s = lines[index++] + "\n";
        char[] data = s.toCharArray();
        System.arraycopy(data, 0, cbuf, off, Math.min(len, data.length));
        return Math.min(len, data.length);
      }
      String s = lines[index++] + "\n";
      char[] data = s.toCharArray();
      System.arraycopy(data, 0, cbuf, off, Math.min(len, data.length));
      return Math.min(len, data.length);
    }
    return -1;
  }

  @Override
  public void close() throws IOException {
    closed = true;
    if (failOnClose) {
      throw new IOException("close failed");
    }
  }

  public boolean closed() {
    return closed;
  }
}
```

## Задание 15

### CourseCode.java (задача 15)

```java
import java.util.Locale;

// Код курса приходит из разных источников ("java-101", " JAVA_101 ", "Java 101"),
// но в системе он должен быть один. Нормализация убирает пробелы и разделители,
// приводит к верхнему регистру и только потом проверяется:
// порядок важен, иначе "   " превратится в валидный код.
public class CourseCode {
  public static final int MIN_LENGTH = 3;
  public static final int MAX_LENGTH = 10;

  private final String value;

  private CourseCode(String value) {
    this.value = value;
  }

  public static CourseCode parse(String raw) {
    // null - это ошибка программиста, а не плохие данные:
    // для данных есть IllegalArgumentException с причиной
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
      boolean latinLetterOrDigit =
          (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
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

### CourseCodeTest.java (задача 15)

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CourseCodeTest {
  @ParameterizedTest(name = "normalize(\"{0}\") -> \"{1}\"")
  @CsvSource({
      "JAVA101,              JAVA101",
      "' java-101 ',         JAVA101",
      "'java_101',           JAVA101",
      "'Java 101',           JAVA101",
      "'  java   basics ', JAVABASICS"
  })
  void normalizesRawCodeToCanonicalForm(String raw, String expected) {
    CourseCode code = CourseCode.parse(raw);
    assertEquals(expected, code.value());
  }

  @ParameterizedTest(name = "parse(\"{0}\") -> IllegalArgumentException: {1}")
  @MethodSource("invalidCodes")
  void rejectsInvalidCode(String raw, String expectedMessage) {
    IllegalArgumentException e = assertThrows(
        IllegalArgumentException.class,
        () -> CourseCode.parse(raw));
    assertEquals(expectedMessage, e.getMessage());
  }

  static Stream<Arguments> invalidCodes() {
    return Stream.of(
        Arguments.of("", "code must not be blank"),
        Arguments.of("   ", "code must not be blank"),
        Arguments.of("-_-_", "code must not be blank"),
        Arguments.of("ab", "code length must be in [3, 10]"),
        Arguments.of("ABCDEFGHIJK", "code length must be in [3, 10]"),
        Arguments.of("JAVA101!", "code must contain only latin letters and digits, got '!'"));
  }

  @Test
  void acceptsBoundaryLengths() {
    assertEquals("AAA", CourseCode.parse("AAA").value());
    String longest = "B".repeat(10);
    assertEquals(longest, CourseCode.parse(longest).value());
  }
}
```

### CourseHub.java (задача 15)

```java
import src.Course;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class CourseHub {
  private final CourseImporter importer;
  private final CourseStore store;
  private final EnrollmentService enrollment;

  public CourseHub(CourseImporter importer, CourseStore store, EnrollmentService enrollment) {
    this.importer = importer;
    this.store = store;
    this.enrollment = enrollment;
  }

  public static class ImportResult {
    private final int processed;
    private final ErrorCode error;
    private final String message;
    private final Throwable cause;

    private ImportResult(int processed, ErrorCode error, String message, Throwable cause) {
      this.processed = processed;
      this.error = error;
      this.message = message;
      this.cause = cause;
    }

    public static ImportResult ok(int processed) {
      return new ImportResult(processed, null, null, null);
    }

    public static ImportResult fail(ErrorCode error, String message, Throwable cause) {
      return new ImportResult(0, error, message, cause);
    }

    public int processed() { return processed; }
    public boolean isOk() { return error == null; }
    public ErrorCode errorCode() { return error; }
    public String message() { return message; }
    public Throwable cause() { return cause; }
  }

  public static class EnrollResult {
    private final boolean ok;
    private final ErrorCode error;
    private final String message;
    private final Throwable cause;

    private EnrollResult(boolean ok, ErrorCode error, String message, Throwable cause) {
      this.ok = ok;
      this.error = error;
      this.message = message;
      this.cause = cause;
    }

    public static EnrollResult ok() { return new EnrollResult(true, null, null, null); }
    public static EnrollResult fail(ErrorCode error, String message, Throwable cause) {
      return new EnrollResult(false, error, message, cause);
    }

    public boolean isOk() { return ok; }
    public ErrorCode errorCode() { return error; }
    public String message() { return message; }
    public Throwable cause() { return cause; }
  }

  public ImportResult importCourses(Reader reader) {
    try {
      int p = importer.importFrom(reader);
      return ImportResult.ok(p);
    } catch (CourseImportException e) {
      return ImportResult.fail(ErrorCode.IMPORT_MALFORMED_RECORD, e.getMessage(), e);
    } catch (IOException e) {
      return ImportResult.fail(ErrorCode.IMPORT_IO_ERROR, e.getMessage(), e.getCause());
    } catch (IllegalArgumentException e) {
      return ImportResult.fail(ErrorCode.HUB_INVALID_ARGUMENT, e.getMessage(), e.getCause());
    } catch (IllegalStateException e) {
      return ImportResult.fail(ErrorCode.HUB_UNEXPECTED, e.getMessage(), e.getCause());
    } catch (Exception e) {
      // Глобальный catch (Exception) на публичном слое запрещён политикой безопасности/границ.
      // Здесь не ловим Exception намеренно: инфраструктурные ошибки — IOException,
      // доменные — свои checked, программные — unchecked не ловим как "успешный" отказ.
      throw new RuntimeException("unexpected exception at import boundary", e);
    }
  }

  public EnrollResult enroll(long studentId, long courseId) {
    try {
      Course course = store.get(courseId);
      if (course == null) {
        return EnrollResult.fail(ErrorCode.HUB_INVALID_ARGUMENT,
            "course " + courseId + " not found", null);
      }
      enrollment.enroll(course, studentId);
      return EnrollResult.ok();
    } catch (EnrollmentRejectedException e) {
      ErrorCode code = switch (e.reasonCode()) {
        case COURSE_CLOSED -> ErrorCode.ENROLL_CLOSED;
        case COURSE_FULL -> ErrorCode.ENROLL_FULL;
        case PREREQUISITE_MISSING -> ErrorCode.ENROLL_PREREQ_MISSING;
        case ALREADY_ENROLLED -> ErrorCode.ENROLL_ALREADY;
      };
      return EnrollResult.fail(code, e.getMessage(), e.getCause());
    } catch (IllegalArgumentException e) {
      return EnrollResult.fail(ErrorCode.HUB_INVALID_ARGUMENT, e.getMessage(), e.getCause());
    } catch (IllegalStateException e) {
      return EnrollResult.fail(ErrorCode.HUB_UNEXPECTED, e.getMessage(), e.getCause());
    } catch (Exception e) {
      throw new RuntimeException("unexpected exception at enroll boundary", e);
    }
  }
}
```

### CourseHubBoundaryTest.java (задача 15)

```java
import org.junit.jupiter.api.Test;
import src.Course;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

class CourseHubBoundaryTest {
  @Test
  void hubTranslatesDomainErrorsToStableErrorCodes() {
    CourseStore store = new CourseStore();
    // preload course CLOSED
    Course closed = new Course(1, "Java", 32);
    closed.setStatus(Course.Status.CLOSED);
    closed.setCapacity(5);
    closed.setHasPrerequisite(true);
    store.publish(java.util.List.of(closed));

    CourseHub hub = new CourseHub(new CourseImporter(store), store, new EnrollmentService());
    CourseHub.EnrollResult r = hub.enroll(42, 1);
    assertFalse(r.isOk());
    assertEquals(ErrorCode.ENROLL_CLOSED, r.errorCode());
    assertNotNull(r.message());
  }

  @Test
  void hubReturnsImportMalformedWithCausePreserved() {
    CourseStore store = new CourseStore();
    CourseHub hub = new CourseHub(new CourseImporter(store), store, new EnrollmentService());
    CourseHub.ImportResult r = hub.importCourses(new StringReader("1;Java;bad"));
    assertFalse(r.isOk());
    assertEquals(ErrorCode.IMPORT_MALFORMED_RECORD, r.errorCode());
    assertNotNull(r.cause());
    assertInstanceOf(CourseImportException.class, r.cause());
    // cause сохраняется
    assertTrue(r.cause() != null);
  }

  @Test
  void hubReturnsImportMalformedOnBadRecord() {
    CourseStore store = new CourseStore();
    CourseHub hub = new CourseHub(new CourseImporter(store), store, new EnrollmentService());
    CourseHub.ImportResult r = hub.importCourses(new StringReader("1;Java;32\n2;Kotlin;oops"));
    assertFalse(r.isOk());
    assertEquals(ErrorCode.IMPORT_MALFORMED_RECORD, r.errorCode());
  }

  @Test
  void hubEnrollFull() {
    CourseStore store = new CourseStore();
    Course c = new Course(1, "Java", 32);
    c.setStatus(Course.Status.OPEN);
    c.setCapacity(0);
    c.setHasPrerequisite(true);
    store.publish(java.util.List.of(c));
    CourseHub hub = new CourseHub(new CourseImporter(store), store, new EnrollmentService());
    CourseHub.EnrollResult r = hub.enroll(42, 1);
    assertFalse(r.isOk());
    assertEquals(ErrorCode.ENROLL_FULL, r.errorCode());
  }

  @Test
  void hubEnrollPrereqMissing() {
    CourseStore store = new CourseStore();
    Course c = new Course(1, "Java", 32);
    c.setStatus(Course.Status.OPEN);
    c.setCapacity(5);
    c.setHasPrerequisite(false);
    store.publish(java.util.List.of(c));
    CourseHub hub = new CourseHub(new CourseImporter(store), store, new EnrollmentService());
    CourseHub.EnrollResult r = hub.enroll(42, 1);
    assertFalse(r.isOk());
    assertEquals(ErrorCode.ENROLL_PREREQ_MISSING, r.errorCode());
  }

  @Test
  void hubEnrollAlready() throws EnrollmentRejectedException {
    CourseStore store = new CourseStore();
    Course c = new Course(1, "Java", 32);
    c.setStatus(Course.Status.OPEN);
    c.setCapacity(5);
    c.setHasPrerequisite(true);
    c.addEnrolled(42);
    store.publish(java.util.List.of(c));
    CourseHub hub = new CourseHub(new CourseImporter(store), store, new EnrollmentService());
    CourseHub.EnrollResult r = hub.enroll(42, 1);
    assertFalse(r.isOk());
    assertEquals(ErrorCode.ENROLL_ALREADY, r.errorCode());
  }

  @Test
  void hubImportSuccess() {
    CourseStore store = new CourseStore();
    CourseHub hub = new CourseHub(new CourseImporter(store), store, new EnrollmentService());
    CourseHub.ImportResult r = hub.importCourses(new StringReader("1;Java;32\n"));
    assertTrue(r.isOk());
    assertEquals(1, r.processed());
  }

  @Test
  void hubEnrollNotFoundGivesInvalidArgument() {
    CourseStore store = new CourseStore();
    CourseHub hub = new CourseHub(new CourseImporter(store), store, new EnrollmentService());
    CourseHub.EnrollResult r = hub.enroll(42, 999);
    assertFalse(r.isOk());
    assertEquals(ErrorCode.HUB_INVALID_ARGUMENT, r.errorCode());
  }

  @Test
  void resourcesClosedOnImport_malformedWithCloseFailure() {
    String[] lines = {"1;Java;32", "bad"};
    FailingReader reader = new FailingReader(lines, 2, true);
    CourseStore store = new CourseStore();
    CourseHub hub = new CourseHub(new CourseImporter(store), store, new EnrollmentService());
    CourseHub.ImportResult r = hub.importCourses(reader);
    assertFalse(r.isOk());
    assertTrue(reader.closed());
  }

  @Test
  void hubImportIoOnClose() {
    String[] lines = {"1;Java;32"};
    FailingReader reader = new FailingReader(lines, 100, true);
    CourseStore store = new CourseStore();
    CourseHub hub = new CourseHub(new CourseImporter(store), store, new EnrollmentService());
    CourseHub.ImportResult r = hub.importCourses(reader);
    assertFalse(r.isOk());
    assertEquals(ErrorCode.IMPORT_IO_ERROR, r.errorCode());
    assertTrue(reader.closed());
  }
}
```

### CourseImportException.java (задача 15)

```java
public class CourseImportException extends Exception {
  private final int recordNumber;

  public CourseImportException(int recordNumber, String message, Throwable cause) {
    super(message, cause);
    this.recordNumber = recordNumber;
  }

  public int recordNumber() {
    return recordNumber;
  }
}
```

### CourseImporter.java (задача 15)

```java
import src.Course;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

// Импортёр читает записи построчно. Если запись сломана — бросает
// CourseImportException с номером записи и cause. Если закрылся ресурс —
// IOException в close превращается в suppressed (try-with-resources).
// Никакой частичной публикации: данные накапливаются во временном списке,
// публикуем ТОЛЬКО в конце, когда все записи успешно прочитаны.
public class CourseImporter {
  private final CourseStore store;

  public CourseImporter(CourseStore store) {
    this.store = store;
  }

  public int importFrom(Reader reader) throws IOException, CourseImportException {
    if (reader == null) throw new IllegalArgumentException("reader must not be null");
    List<Course> buffer = new ArrayList<>();
    BufferedReader br = new BufferedReader(reader);
    String line;
    int recordNumber = 0;
    try (br) {
      while ((line = br.readLine()) != null) {
        recordNumber++;
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
        try {
          Course c = parseRecord(trimmed, recordNumber);
          buffer.add(c);
        } catch (Exception ex) {
          throw new CourseImportException(recordNumber,
              "Invalid record at line " + recordNumber, ex);
        }
      }
      // публикация только целиком: buffer содержит все успешно прочитанные записи
      store.publish(buffer);
      return buffer.size();
    } catch (CourseImportException e) {
      throw e;
    } catch (IOException ex) {
      throw ex;
    } catch (Throwable ex) {
      throw new CourseImportException(recordNumber,
          "Invalid record at line " + recordNumber, ex);
    }
  }

  private Course parseRecord(String line, int recordNumber) {
    // формат: id;title;hours
    String[] parts = line.split(";", 3);
    if (parts.length != 3) {
      throw new IllegalArgumentException("bad format");
    }
    long id = Long.parseLong(parts[0].trim());
    String title = parts[1].trim();
    int hours = Integer.parseInt(parts[2].trim());
    return new Course(id, title, hours);
  }
}
```

### CourseImporterTest.java (задача 15)

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import src.Course;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CourseImporterTest {
  @Test
  void importsValid() throws Exception {
    CourseStore store = new CourseStore();
    CourseImporter importer = new CourseImporter(store);
    StringReader reader = new StringReader("1;Java;32\n2;Kotlin;24\n");
    int processed = importer.importFrom(reader);
    assertEquals(2, processed);
    assertEquals(2, store.size());
  }

  @Test
  void malformedThrowsWithRecordNumberAndCause() {
    CourseStore store = new CourseStore();
    CourseImporter importer = new CourseImporter(store);
    StringReader reader = new StringReader("1;Java;32\n2;Kotlin;bad\n");
    CourseImportException e = assertThrows(CourseImportException.class,
        () -> importer.importFrom(reader));
    assertEquals(2, e.recordNumber());
    assertNotNull(e.getCause());
    assertEquals(0, store.size());
  }

  @Test
  void partialNotPublished() {
    CourseStore store = new CourseStore();
    CourseImporter importer = new CourseImporter(store);
    StringReader reader = new StringReader("1;Java;32\nbad\n2;Kotlin;24\n");
    assertThrows(CourseImportException.class, () -> importer.importFrom(reader));
    assertEquals(0, store.size());
  }

  @Test
  void malformedAndCloseFailure_suppressed() {
    String[] lines = {"1;Java;32", "2;bad"};
    FailingReader reader = new FailingReader(lines, 2, true);
    CourseStore store = new CourseStore();
    CourseImporter importer = new CourseImporter(store);
    CourseImportException e = assertThrows(CourseImportException.class,
        () -> importer.importFrom(reader));
    assertEquals(2, e.recordNumber());
    assertEquals(1, e.getSuppressed().length);
    assertTrue(e.getSuppressed()[0] instanceof java.io.IOException);
    assertTrue(reader.closed());
    assertEquals(0, store.size());
  }

  @Test
  void closeFailureOnly_io() {
    String[] lines = {"1;Java;32"};
    FailingReader reader = new FailingReader(lines, 100, true);
    CourseStore store = new CourseStore();
    CourseImporter importer = new CourseImporter(store);
    assertThrows(java.io.IOException.class, () -> importer.importFrom(reader));
    assertTrue(reader.closed());
  }
}
```

### CourseStore.java (задача 15)

```java
import java.util.LinkedHashMap;
import java.util.Map;

import src.Course;

// CourseStore публикует результат только целиком.
// Записи не добавляются частично: либо импорт успешен, либо ничего не изменилось.
public class CourseStore {
  private final Map<Long, Course> courses = new LinkedHashMap<>();

  public void publish(java.util.Collection<Course> imported) {
    if (imported == null) throw new IllegalArgumentException("imported must not be null");
    // атомарная публикация: сначала проверяем, что можно опубликовать,
    // затем заменяем содержимое
    Map<Long, Course> copy = new LinkedHashMap<>(imported.size());
    for (Course c : imported) {
      if (copy.putIfAbsent(c.id(), c) != null) {
        throw new IllegalArgumentException("duplicate id: " + c.id());
      }
    }
    courses.clear();
    courses.putAll(copy);
  }

  public Course get(long id) {
    return courses.get(id);
  }

  public int size() {
    return courses.size();
  }

  public void clear() {
    courses.clear();
  }
}
```

### CourseTest.java (задача 15)

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import src.Course;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Базовый тест Course: дополнительно проверим валидации
class CourseTest {
  @Test
  void validCourse() {
    Course c = new Course(1, "Java", 32);
    assertEquals(1, c.id());
    assertEquals("Java", c.title());
    assertEquals(32, c.durationHours());
  }

  @Test
  void rejectsInvalidDuration() {
    assertThrows(IllegalArgumentException.class, () -> new Course(1, "Java", 0));
    assertThrows(IllegalArgumentException.class, () -> new Course(1, "Java", 101));
  }

  @Test
  void rejectsInvalidTitle() {
    assertThrows(IllegalArgumentException.class, () -> new Course(1, "", 32));
    assertThrows(IllegalArgumentException.class, () -> new Course(1, "   ", 32));
    assertThrows(IllegalArgumentException.class, () -> new Course(1, null, 32));
  }
}
```

### EnrollmentRejectedException.java (задача 15)

```java
public class EnrollmentRejectedException extends Exception {
  public enum ReasonCode {
    COURSE_FULL,
    PREREQUISITE_MISSING,
    ALREADY_ENROLLED,
    COURSE_CLOSED
  }

  private final long studentID;
  private final long courseID;
  private final ReasonCode reasonCode;

  public EnrollmentRejectedException(long studentID, long courseID, 
      ReasonCode reasonCode) {
    super("Enrollment rejected: " + reasonCode);
    this.studentID = studentID;
    this.courseID = courseID;
    this.reasonCode = reasonCode;
  }
  
  public EnrollmentRejectedException(long studentID, long courseID,
      ReasonCode reasonCode, Throwable cause) {
    super("Enrollment rejected: " + reasonCode, cause);
    this.studentID = studentID;
    this.courseID = courseID;
    this.reasonCode = reasonCode;
  }

  public long studentID() { return studentID; }
  public long courseID() { return courseID; }
  public ReasonCode reasonCode() { return reasonCode; }

  @Override
  public String toString() {
    return "EnrollmentRejectedException{reasonCode=" + reasonCode + "}";
  }
}
```

### EnrollmentService.java (задача 15)

```java
import src.Course;

import java.util.Set;

public class EnrollmentService {
  public void enroll(Course course, long studentId) throws EnrollmentRejectedException {
    // порядок проверок фиксируем: сначала статус,
    // затем места, затем prerequisite, затем повторная заявка
    if (course.status() != Course.Status.OPEN) {
      throw new EnrollmentRejectedException(
          studentId, course.id(),
          EnrollmentRejectedException.ReasonCode.COURSE_CLOSED);
    }
    if (course.enrolledCount() >= course.capacity()) {
      throw new EnrollmentRejectedException(
          studentId, course.id(),
          EnrollmentRejectedException.ReasonCode.COURSE_FULL);
    }
    if (!course.hasPrerequisite()) {
      throw new EnrollmentRejectedException(
          studentId, course.id(),
          EnrollmentRejectedException.ReasonCode.PREREQUISITE_MISSING);
    }
    // повторная заявка
    if (course.enrolledStudents().contains(studentId)) {
      throw new EnrollmentRejectedException(
          studentId, course.id(),
          EnrollmentRejectedException.ReasonCode.ALREADY_ENROLLED);
    }
    course.addEnrolled(studentId);
  }
}
```

### EnrollmentServiceTest.java (задача 15)

```java
import org.junit.jupiter.api.Test;
import src.Course;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EnrollmentServiceTest {
  @Test
  void enrollSuccess() throws EnrollmentRejectedException {
    Course course = new Course(1, "Java", 32);
    course.setStatus(Course.Status.OPEN);
    course.setCapacity(5);
    course.setHasPrerequisite(true);
    EnrollmentService s = new EnrollmentService();
    s.enroll(course, 42);
    assertEquals(1, course.enrolledCount());
  }

  @Test
  void rejectClosed() {
    Course course = new Course(1, "Java", 32);
    course.setStatus(Course.Status.CLOSED);
    course.setCapacity(5);
    course.setHasPrerequisite(true);
    EnrollmentService s = new EnrollmentService();
    EnrollmentRejectedException e = assertThrows(EnrollmentRejectedException.class,
        () -> s.enroll(course, 42));
    assertEquals(EnrollmentRejectedException.ReasonCode.COURSE_CLOSED, e.reasonCode());
  }

  @Test
  void rejectFull() {
    Course course = new Course(1, "Java", 32);
    course.setStatus(Course.Status.OPEN);
    course.setCapacity(0);
    course.setHasPrerequisite(true);
    EnrollmentService s = new EnrollmentService();
    EnrollmentRejectedException e = assertThrows(EnrollmentRejectedException.class,
        () -> s.enroll(course, 42));
    assertEquals(EnrollmentRejectedException.ReasonCode.COURSE_FULL, e.reasonCode());
  }

  @Test
  void rejectPrereq() {
    Course course = new Course(1, "Java", 32);
    course.setStatus(Course.Status.OPEN);
    course.setCapacity(5);
    course.setHasPrerequisite(false);
    EnrollmentService s = new EnrollmentService();
    EnrollmentRejectedException e = assertThrows(EnrollmentRejectedException.class,
        () -> s.enroll(course, 42));
    assertEquals(EnrollmentRejectedException.ReasonCode.PREREQUISITE_MISSING, e.reasonCode());
  }

  @Test
  void rejectAlready() throws EnrollmentRejectedException {
    Course course = new Course(1, "Java", 32);
    course.setStatus(Course.Status.OPEN);
    course.setCapacity(5);
    course.setHasPrerequisite(true);
    course.addEnrolled(42);
    EnrollmentService s = new EnrollmentService();
    EnrollmentRejectedException e = assertThrows(EnrollmentRejectedException.class,
        () -> s.enroll(course, 42));
    assertEquals(EnrollmentRejectedException.ReasonCode.ALREADY_ENROLLED, e.reasonCode());
  }

  @Test
  void rejectDoesNotChangeState() {
    Course course = new Course(1, "Java", 32);
    course.setStatus(Course.Status.CLOSED);
    course.setCapacity(5);
    course.setHasPrerequisite(true);
    int before = course.enrolledCount();
    EnrollmentService s = new EnrollmentService();
    assertThrows(EnrollmentRejectedException.class, () -> s.enroll(course, 42));
    assertEquals(before, course.enrolledCount());
  }
}
```

### ErrorCode.java (задача 15)

```java
public enum ErrorCode {
  COURSE_INVALID_DURATION,      // 1..100
  COURSE_INVALID_TITLE,         // пустой заголовок
  COURSE_INVALID_COMPLETED,     // completed > duration или < 0
  COURSE_CODE_BLANK,
  COURSE_CODE_BAD_LENGTH,
  COURSE_CODE_BAD_CHARS,
  IMPORT_MALFORMED_RECORD,
  IMPORT_IO_ERROR,
  ENROLL_CLOSED,
  ENROLL_FULL,
  ENROLL_PREREQ_MISSING,
  ENROLL_ALREADY,
  STORE_DUPLICATE_ID,
  HUB_INVALID_ARGUMENT,
  HUB_UNEXPECTED
}
```

### FailingReader.java (задача 15)

```java
import java.io.IOException;
import java.io.Reader;

public class FailingReader extends Reader {
  private final String[] lines;
  private final int malformedAt;
  private int index;
  private final boolean failOnClose;
  private boolean closed;

  public FailingReader(String[] lines, int malformedAt, boolean failOnClose) {
    this.lines = lines == null ? new String[0] : lines;
    this.malformedAt = malformedAt;
    this.failOnClose = failOnClose;
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    if (index < lines.length) {
      if (malformedAt >= 1 && index == malformedAt - 1) {
        // Содержимое "битой" записи отдаём целиком, чтобы
        // BufferedReader прочитал её как строку №3.
        // Ошибку записи будем симулировать позже при парсинге? Но мы контролируем строку.
        // Просто отдадим строку и выставим флаг: при следующем? Или лучше отдать строку,
        // но в импортере мы уже обработали предыдущие — строка будет прочитана.
        String s = lines[index++] + "\n";
        char[] data = s.toCharArray();
        System.arraycopy(data, 0, cbuf, off, Math.min(len, data.length));
        return Math.min(len, data.length);
      }
      String s = lines[index++] + "\n";
      char[] data = s.toCharArray();
      System.arraycopy(data, 0, cbuf, off, Math.min(len, data.length));
      return Math.min(len, data.length);
    }
    return -1;
  }

  @Override
  public void close() throws IOException {
    closed = true;
    if (failOnClose) {
      throw new IOException("close failed");
    }
  }

  public boolean closed() {
    return closed;
  }
}
```

