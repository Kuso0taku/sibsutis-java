# Практическая работа №1 — листинги

Полные исходные листинги всех заданий. В заголовке каждого листинга указан путь к файлу; в скобках — задание (задания), в которых этот файл используется.

> Файлы `task6/BrokenOverride.java` и `task10/BrokenDefaults.java` намеренно не компилируются (исключены из сборки) и приведены как материал для разбора ошибок.

## Содержание

- Общие файлы (пакет src)
- Задание 1. Один контракт, две реализации
- Задание 2. Dynamic dispatch на бумаге
- Задание 3. Полиморфный массив
- Задание 4. Абстрактный базовый класс
- Задание 5. Интерфейс или абстрактный класс
- Задание 6. Переопределение без случайной перегрузки
- Задание 7. Композиция вместо наследования
- Задание 8. Нарушение LSP
- Задание 9. Политика допуска как стратегия
- Задание 10. Конфликт default-методов
- Задание 11. Равенство в иерархии
- Задание 12. Sealed-иерархия результата
- Задание 13. Удаление type code
- Задание 14. Наследование взрывает комбинации
- Задание 15. Расширяемый движок правил

---

## Общие файлы (пакет src)

### src/Course.java (задания 1, 3, 8, 9, 13, 15)

```java
package src;

// общая модель курса: не меняется между заданиями, лежит в src/
public class Course {
  public enum Status { OPEN, CLOSED }

  private final long id;
  private final String title;
  private int durationHours;

  private Status status = Status.OPEN;
  private int capacity = 30;
  private boolean hasPrerequisite;
  private final java.util.LinkedHashSet<Long> enrolled = new java.util.LinkedHashSet<>();

  public Course(long id, String title, int durationHours) {
    if (id <= 0) throw new IllegalArgumentException("id must be > 0");
    if (title == null || title.isBlank()) throw new IllegalArgumentException("title must not be blank");
    if (durationHours <= 0) throw new IllegalArgumentException("durationHours must be > 0");
    this.id = id;
    this.title = title.trim();
    this.durationHours = durationHours;
  }

  public long id() { return id; }
  public String title() { return title; }
  public int durationHours() { return durationHours; }

  // поля под задачи демонстрации
  public void setStatus(Status status) { this.status = status; }
  public Status status() { return status; }
  public void setCapacity(int capacity) { this.capacity = capacity; }
  public int capacity() { return capacity; }
  public void setHasPrerequisite(boolean has) { this.hasPrerequisite = has; }
  public boolean hasPrerequisite() { return hasPrerequisite; }

  public java.util.Set<Long> enrolledStudents() {
    return java.util.Collections.unmodifiableSet(enrolled);
  }

  public void addEnrolled(long studentId) { enrolled.add(studentId); }
  public int enrolledCount() { return enrolled.size(); }
  public int seatsLeft() { return capacity - enrolledCount(); }
}
```

### src/Student.java (задания 9, 15)

```java
package src;

// общая модель студента: используется в task9 и task15
public class Student {
  private final long id;
  private final String name;
  private final int age;
  private final boolean hasPrerequisite;

  public Student(long id, String name, int age, boolean hasPrerequisite) {
    if (id <= 0) throw new IllegalArgumentException("id must be > 0");
    if (name == null || name.isBlank()) throw new IllegalArgumentException("name must not be blank");
    if (age <= 0) throw new IllegalArgumentException("age must be > 0");
    this.id = id;
    this.name = name.trim();
    this.age = age;
    this.hasPrerequisite = hasPrerequisite;
  }

  public long id() { return id; }
  public String name() { return name; }
  public int age() { return age; }
  public boolean hasPrerequisite() { return hasPrerequisite; }
}
```

---

## Задание 1. Один контракт, две реализации

### task1/CourseFormatter.java (задания 1, 3)

```java
package task1;

import src.Course;

// один контракт, две реализации
public interface CourseFormatter {
  String format(Course course);
}
```

### task1/CompactCourseFormatter.java (задания 1, 3)

```java
package task1;

import src.Course;

// компактный формат: id + название
public class CompactCourseFormatter implements CourseFormatter {
  @Override
  public String format(Course course) {
    return course.id() + " " + course.title();
  }
}
```

### task1/DetailedCourseFormatter.java (задания 1, 3)

```java
package task1;

import src.Course;

// подробный формат: название, часы, статус
public class DetailedCourseFormatter implements CourseFormatter {
  @Override
  public String format(Course course) {
    return course.title() + " (" + course.durationHours() + " h, " + course.status() + ")";
  }
}
```

### task1/Task1Demo.java (задание 1)

```java
package task1;

import src.Course;

// компилятор знает только CourseFormatter.format(Course);
// какая реализация вызовется — решается во время выполнения по классу объекта
public class Task1Demo {
  public static void main(String[] args) {
    Course course = new Course(1, "Java", 10);

    // обе реализации через переменную интерфейсного типа
    CourseFormatter compact = new CompactCourseFormatter();
    CourseFormatter detailed = new DetailedCourseFormatter();

    System.out.println(compact.format(course));
    System.out.println(detailed.format(course));
  }
}
```

---

## Задание 2. Dynamic dispatch на бумаге

### task2/NotificationSender.java (задания 2, 4, 6, 7)

```java
package task2;

// контракт отправки уведомления
public interface NotificationSender {
  void send(String recipient, String message);
}
```

### task2/EmailSender.java (задания 2, 6, 7)

```java
package task2;

// email-реализация
public class EmailSender implements NotificationSender {
  @Override
  public void send(String recipient, String message) {
    System.out.println("email to " + recipient + ": " + message);
  }
}
```

### task2/ConsoleSender.java (задание 2)

```java
package task2;

// console-реализация
public class ConsoleSender implements NotificationSender {
  @Override
  public void send(String recipient, String message) {
    System.out.println("console[" + recipient + "]: " + message);
  }
}
```

### task2/Task2Demo.java (задание 2)

```java
package task2;

// разделение overload resolution (compile-time типы) и override dispatch (runtime класс)
public class Task2Demo {

  // перегруженные методы: какая перегрузка вызовется — решается по compile-time типу
  static String describe(NotificationSender sender) { return "by interface"; }
  static String describe(EmailSender sender) { return "by concrete"; }

  // override dispatch: внутри метода вызовется метод фактического класса объекта
  static void report(NotificationSender sender) {
    sender.send("student@example.com", "курс открыт");
    // перегрузка выбирается по типу параметра, а не по классу объекта
    System.out.println("  overload -> " + describe(sender));
  }

  public static void main(String[] args) {
    NotificationSender asInterface = new EmailSender();
    EmailSender asConcrete = new EmailSender();
    NotificationSender console = new ConsoleSender();

    report(asInterface); // send -> EmailSender, describe -> "by interface"
    report(console);     // send -> ConsoleSender, describe -> "by interface"
    report(asConcrete);  // send -> EmailSender, describe -> "by interface"

    System.out.println(describe(asInterface)); // by interface
    System.out.println(describe(asConcrete));  // by concrete
  }
}
```

---

## Задание 3. Полиморфный массив

Использует файлы задания 1: `CourseFormatter`, `CompactCourseFormatter`, `DetailedCourseFormatter` (листинги выше).

### task3/HtmlCourseFormatter.java (задание 3)

```java
package task3;

import src.Course;
import task1.CourseFormatter;

// третья реализация: добавляется без изменения обхода в Task3Demo
public class HtmlCourseFormatter implements CourseFormatter {
  @Override
  public String format(Course course) {
    return "<b>" + course.title() + "</b>";
  }
}
```

### task3/Task3Demo.java (задание 3)

```java
package task3;

import src.Course;
import task1.CompactCourseFormatter;
import task1.CourseFormatter;
import task1.DetailedCourseFormatter;

// клиент зависит только от интерфейса: ни одного if по типу
public class Task3Demo {
  public static void main(String[] args) {
    Course course = new Course(1, "Java", 10);

    // массив по типу интерфейса
    CourseFormatter[] formatters = {
        new CompactCourseFormatter(),
        new DetailedCourseFormatter(),
        new HtmlCourseFormatter()
    };

    // обход одинаков для любой реализации
    for (CourseFormatter formatter : formatters) {
      System.out.println(formatter.format(course));
    }
  }
}
```

---

## Задание 4. Абстрактный базовый класс

Реализует `task2.NotificationSender` из задания 2 (листинг выше).

### task4/AbstractNotificationSender.java (задание 4)

```java
package task4;

import task2.NotificationSender;

// абстрактный базовый класс: общая проверка адресата + точка расширения doSend
public abstract class AbstractNotificationSender implements NotificationSender {

  // публичный алгоритм фиксирован: сначала проверка, потом шаг реализации
  @Override
  public final void send(String recipient, String message) {
    if (recipient == null || recipient.isBlank()) {
      throw new IllegalArgumentException("recipient must not be blank");
    }
    doSend(recipient, message);
  }

  // точка расширения: подклассы решают, как именно отправлять
  protected abstract void doSend(String recipient, String message);
}
```

### task4/EmailNotificationSender.java (задание 4)

```java
package task4;

// email-вариант: только шаг отправки
public class EmailNotificationSender extends AbstractNotificationSender {
  @Override
  protected void doSend(String recipient, String message) {
    System.out.println("email to " + recipient + ": " + message);
  }
}
```

### task4/ConsoleNotificationSender.java (задание 4)

```java
package task4;

// console-вариант: только шаг отправки
public class ConsoleNotificationSender extends AbstractNotificationSender {
  @Override
  protected void doSend(String recipient, String message) {
    System.out.println("console[" + recipient + "]: " + message);
  }
}
```

### task4/Task4Demo.java (задание 4)

```java
package task4;

// send() — final: проверка адресата общая, менять её нельзя;
// doSend() — abstract: единственное место для подклассов
public class Task4Demo {
  public static void main(String[] args) {
    AbstractNotificationSender email = new EmailNotificationSender();
    AbstractNotificationSender console = new ConsoleNotificationSender();

    // общий алгоритм: проверка + делегирование doSend конкретного класса
    email.send("student@example.com", "Java стартует");
    console.send("student@example.com", "Java стартует");

    try {
      email.send("  ", "никогда не отправится");
    } catch (IllegalArgumentException e) {
      System.out.println("проверка сработала: " + e.getMessage());
    }
  }
}
```

---

## Задание 5. Интерфейс или абстрактный класс

### task5/answer.md (задание 5)

```markdown
# Интерфейс, абстрактный класс или обычный класс

| Понятие | Выбор | Мотивировка | Цена изменения |
|---|---|---|---|
| `Identifiable` | `interface` | Это не «тип с состоянием», а обещание `id()`. Подписаться может кто угодно: сущность, record, даже enum. Иерархия наследования не должна мешать. | Малая: новых implementors не ломает существующий код. |
| `CoursePolicy` | `interface` | Стратегия без состояния: только метод решения. Легко комбинировать (`allOf`, `anyOf`), легко подменить. | Малая: новая политика — новый класс, `EnrollmentService` не меняется. |
| `BaseEntity` | `abstract class` | Есть общее изменяемое состояние и поведение (id, equals/hashCode). Нужен один базовый конструктор — это уже наследование состояния. | Высокая: Java не даёт наследовать ещё у кого-то; все сущности привязаны к этой иерархии. |
| `Formatter` | `interface` | Форматирование — чистое поведение без общего состояния. Как и `CourseFormatter` в task1. | Малая: третья реализация не трогает существующие. |
| `ClockProvider` | `interface` (обычно функциональный) | Нужна подмена времени в тестах: `() -> fixedInstant`. Даже абстрактный класс избыточен — нет общего кода. | Минимальная: метод-ссылка, лямбда, mock. |

## Правило выбора

- Есть **общее состояние или общий алгоритм**, который нельзя дублировать → `abstract class` (как `AbstractNotificationSender` в task4).
- Есть только **контракт поведения** → `interface` (task1, task9).
- Есть **только данные, поведение не меняется** → обычный класс / record, отдельный тип не нужен.
- Отдельный тип вообще не нужен, когда хватает лямбды (`ClockProvider`).
```

---

## Задание 6. Переопределение без случайной перегрузки

Использует `task2.EmailSender` и `task2.NotificationSender` из задания 2 (листинги выше).

### task6/BrokenOverride.java (задание 6)

```java
package task6;

// Намеренно не компилируется (исключено из pom.xml).
// Ошибки: чужой параметр при @Override, сужение доступа, несвязанный возвращаемый тип.
// Запуск вручную: javac pract1/task6/BrokenOverride.java

class Base {
  void send(String to) { System.out.println("base.send"); }
  void log(String msg) { System.out.println("base.log"); }
  Number value() { return 1; }
}

class Broken extends Base {
  // ошибка: другой параметр — это новая перегрузка, @Override нечего переопределять
  @Override
  void send(int to) { System.out.println("broken.send"); }

  // ошибка: нельзя сужать доступ при переопределении (package-private -> private)
  @Override
  private void log(String msg) { System.out.println("broken.log"); }

  // ошибка: String не подтип Number — неcovariant return
  @Override
  String value() { return "1"; }
}
```

### task6/FixedOverride.java (задание 6)

```java
package task6;

// исправленный вариант: сигнтуры совпадают, доступ не сужается,
// возвращаемый тип covariant, везде @Override
class FixedBase {
  void send(String to) { System.out.println("base.send"); }
  protected void log(String msg) { System.out.println("base.log"); }
  Number value() { return 1; }
}

class Fixed extends FixedBase {
  @Override
  void send(String to) { System.out.println("fixed.send to " + to); }

  @Override
  protected void log(String msg) { System.out.println("fixed.log: " + msg); }

  @Override
  Integer value() { return 2; } // covariant: Integer — подтип Number
}

public class FixedOverride {
  public static void main(String[] args) {
    FixedBase obj = new Fixed();
    obj.send("a");
    obj.log("b");
    System.out.println(obj.value());
  }
}
```

### task6/Task6Demo.java (задание 6)

```java
package task6;

import task2.EmailSender;
import task2.NotificationSender;

// компилируется без @Override, но вызывается не тот метод:
// в Sub это перегрузка, а не переопределение
class HandleBase {
  void handle(NotificationSender sender) { System.out.println("base.handle(NotificationSender)"); }
}

class HandleSub extends HandleBase {
  // хотелось переопределить, а получилась перегрузка — @Override показал бы ошибку
  void handle(EmailSender sender) { System.out.println("sub.handle(EmailSender)"); }
}

public class Task6Demo {
  public static void main(String[] args) {
    // compile-time тип HandleBase -> вызовется base.handle, хотя класс объекта HandleSub
    HandleBase byBaseType = new HandleSub();
    byBaseType.handle(new EmailSender());

    // compile-time тип HandleSub -> перегрузка по EmailSender
    HandleSub bySubType = new HandleSub();
    bySubType.handle(new EmailSender());
  }
}
```

---

## Задание 7. Композиция вместо наследования

Оборачивает `task2.EmailSender`, реализуя `task2.NotificationSender` (задание 2).

### task7/LoggingDecorator.java (задание 7)

```java
package task7;

import task2.NotificationSender;

// декоратор: логирует факт отправки, делегирует работу внутрь
public class LoggingDecorator implements NotificationSender {
  private final NotificationSender delegate;

  public LoggingDecorator(NotificationSender delegate) {
    this.delegate = delegate;
  }

  @Override
  public void send(String recipient, String message) {
    System.out.println("log: start send to " + recipient);
    delegate.send(recipient, message);
    System.out.println("log: end send to " + recipient);
  }
}
```

### task7/MetricsDecorator.java (задание 7)

```java
package task7;

import task2.NotificationSender;

// декоратор: считает отправки, делегирует работу внутрь
public class MetricsDecorator implements NotificationSender {
  private final NotificationSender delegate;
  private int sendCount;

  public MetricsDecorator(NotificationSender delegate) {
    this.delegate = delegate;
  }

  @Override
  public void send(String recipient, String message) {
    sendCount++;
    System.out.println("metrics: send #" + sendCount);
    delegate.send(recipient, message);
  }

  public int sendCount() { return sendCount; }
}
```

### task7/Task7Demo.java (задание 7)

```java
package task7;

import task2.EmailSender;
import task2.NotificationSender;

// вместо LoggingEmailSender extends EmailSender — композиция:
// порядок декораторов меняет наблюдаемое поведение
public class Task7Demo {
  public static void main(String[] args) {
    // порядок 1: сначала метрики, потом логирование
    System.out.println("== metrics -> logging ==");
    NotificationSender order1 =
        new MetricsDecorator(new LoggingDecorator(new EmailSender()));
    order1.send("a@example.com", "письмо 1");
    order1.send("a@example.com", "письмо 2");

    // порядок 2: сначала логирование, потом метрики
    System.out.println("== logging -> metrics ==");
    NotificationSender order2 =
        new LoggingDecorator(new MetricsDecorator(new EmailSender()));
    order2.send("b@example.com", "письмо 1");
  }
}
```

---

## Задание 8. Нарушение LSP

### task8/CourseReadRepository.java (задание 8)

```java
package task8;

import src.Course;
import java.util.Optional;

// только чтение: контракт не обещает запись
public interface CourseReadRepository {
  Optional<Course> findById(long id);
}
```

### task8/CourseWriteRepository.java (задание 8)

```java
package task8;

import src.Course;

// только запись: отдельный контракт
public interface CourseWriteRepository {
  void save(Course course);
}
```

### task8/CourseRepository.java (задание 8)

```java
package task8;

// полный контракт = чтение + запись
public interface CourseRepository extends CourseReadRepository, CourseWriteRepository {
}
```

### task8/InMemoryCourseRepository.java (задание 8)

```java
package task8;

import src.Course;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// обычная реализация: умеет и читать, и писать
public class InMemoryCourseRepository implements CourseRepository {
  private final Map<Long, Course> storage = new HashMap<>();

  @Override
  public Optional<Course> findById(long id) {
    return Optional.ofNullable(storage.get(id));
  }

  @Override
  public void save(Course course) {
    storage.put(course.id(), course);
  }
}
```

### task8/ReadOnlyCourseRepository.java (задание 8)

```java
package task8;

import src.Course;
import java.util.Optional;

// read-only: реализует только чтение, save вызвать просто нельзя —
// контракт подтипа не обещает невозможного (LSP восстановлен)
public class ReadOnlyCourseRepository implements CourseReadRepository {
  private final CourseReadRepository delegate;

  public ReadOnlyCourseRepository(CourseReadRepository delegate) {
    this.delegate = delegate;
  }

  @Override
  public Optional<Course> findById(long id) {
    return delegate.findById(id);
  }
}
```

### task8/Task8Demo.java (задание 8)

```java
package task8;

import src.Course;

// клиент, которому нужно только чтение, зависит от CourseReadRepository
public class Task8Demo {
  public static void main(String[] args) {
    InMemoryCourseRepository full = new InMemoryCourseRepository();
    full.save(new Course(1, "Java", 10));

    CourseReadRepository readOnly = new ReadOnlyCourseRepository(full);
    System.out.println(readOnly.findById(1).map(Course::title).orElse("not found"));

    // не компилируется — и это правильно: read-only не обещает save
    // readOnly.save(new Course(2, "Go", 8));
  }
}
```

---

## Задание 9. Политика допуска как стратегия

### task9/EnrollmentPolicy.java (задание 9)

```java
package task9;

import src.Course;
import src.Student;

// открытая стратегия: новая политика — новый класс, сервис не меняется
public interface EnrollmentPolicy {
  boolean allows(Student student, Course course);
}
```

### task9/AgePolicy.java (задание 9)

```java
package task9;

import src.Course;
import src.Student;

// политика по возрасту
public class AgePolicy implements EnrollmentPolicy {
  private final int minAge;

  public AgePolicy(int minAge) {
    this.minAge = minAge;
  }

  @Override
  public boolean allows(Student student, Course course) {
    return student.age() >= minAge;
  }
}
```

### task9/PrerequisitePolicy.java (задание 9)

```java
package task9;

import src.Course;
import src.Student;

// политика по пререквизитам: если курс их требует — у студента должен быть опыт
public class PrerequisitePolicy implements EnrollmentPolicy {
  @Override
  public boolean allows(Student student, Course course) {
    return !course.hasPrerequisite() || student.hasPrerequisite();
  }
}
```

### task9/CapacityPolicy.java (задание 9)

```java
package task9;

import src.Course;
import src.Student;

// политика по вместимости
public class CapacityPolicy implements EnrollmentPolicy {
  @Override
  public boolean allows(Student student, Course course) {
    return course.seatsLeft() > 0;
  }
}
```

### task9/EnrollmentService.java (задание 9)

```java
package task9;

import src.Course;
import src.Student;

// сервис зависит только от абстракции EnrollmentPolicy
public class EnrollmentService {
  private final EnrollmentPolicy policy;

  public EnrollmentService(EnrollmentPolicy policy) {
    this.policy = policy;
  }

  public boolean enroll(Student student, Course course) {
    if (!policy.allows(student, course)) {
      return false;
    }
    course.addEnrolled(student.id());
    return true;
  }
}
```

### task9/Task9Demo.java (задание 9)

```java
package task9;

import src.Course;
import src.Student;

// новая политика подключается без изменения EnrollmentService
public class Task9Demo {
  public static void main(String[] args) {
    Course java = new Course(1, "Java", 10);
    java.setHasPrerequisite(true);

    Student adult = new Student(1, "Аня", 20, true);
    Student kid = new Student(2, "Боря", 12, true);
    Student noExp = new Student(3, "Вера", 25, false);

    // комбинация политик через лямбды: сервис тот же
    EnrollmentPolicy combined = (student, course) ->
        new AgePolicy(16).allows(student, course)
            && new PrerequisitePolicy().allows(student, course)
            && new CapacityPolicy().allows(student, course);

    EnrollmentService service = new EnrollmentService(combined);

    System.out.println(service.enroll(adult, java));  // true
    System.out.println(service.enroll(kid, java));    // false: возраст
    System.out.println(service.enroll(noExp, java));  // false: пререквизит

    // ещё одна новая политика — снова без правок сервиса
    EnrollmentPolicy freeOnly = (student, course) -> course.id() % 2 == 0;
    System.out.println(new EnrollmentService(freeOnly).enroll(adult, java)); // false
  }
}
```

---

## Задание 10. Конфликт default-методов

### task10/BrokenDefaults.java (задание 10)

```java
package task10;

// Намеренно не компилируется (исключено из pom.xml).
// Два default-метода с одной сигнатурой — наследование поведения неоднозначно.
// Запуск вручную: javac pract1/task10/BrokenDefaults.java

interface BrokenNotifier {
  default String name() { return "notifier"; }
}

interface BrokenLogger {
  default String name() { return "logger"; }
}

class BrokenNotifierLogger implements BrokenNotifier, BrokenLogger {
  // ошибка компиляции: name() неоднозначен, нужен явный override
}
```

### task10/ResolvedDefaults.java (задание 10)

```java
package task10;

// те же два интерфейса, конфликт разрешён явным override в классе
interface Notifier {
  default String name() { return "notifier"; }
}

interface Logger {
  default String name() { return "logger"; }
}

// множественное наследование поведения требует явного выбора класса
class NotifierLogger implements Notifier, Logger {
  @Override
  public String name() { return "notifier+logger"; }
}
```

### task10/Task10Demo.java (задание 10)

```java
package task10;

// после явного override компилятор молча берёт реализацию класса
public class Task10Demo {
  public static void main(String[] args) {
    NotifierLogger both = new NotifierLogger();
    System.out.println(both.name()); // notifier+logger

    // default-методы интерфейсов доступны по отдельности
    Notifier asNotifier = both;
    Logger asLogger = both;
    // но name() уже переопределён в классе — печатает одинаково
    System.out.println(asNotifier.name());
    System.out.println(asLogger.name());
  }
}
```

---

## Задание 11. Равенство в иерархии

### task11/Point.java (задание 11)

```java
package task11;

// equals сравнивает только координаты — этого мало для иерархии
public class Point {
  final int x;
  final int y;

  Point(int x, int y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Point)) return false;
    Point other = (Point) o;
    return x == other.x && y == other.y;
  }

  @Override
  public int hashCode() {
    return 31 * x + y;
  }

  @Override
  public String toString() {
    return "(" + x + "," + y + ")";
  }
}
```

### task11/ColoredPoint.java (задание 11)

```java
package task11;

// цвет добавлен через наследование: equals стал несимметричным
public class ColoredPoint extends Point {
  final String color;

  ColoredPoint(int x, int y, String color) {
    super(x, y);
    this.color = color;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ColoredPoint)) return false;
    ColoredPoint other = (ColoredPoint) o;
    return x == other.x && y == other.y && color.equals(other.color);
  }

  @Override
  public int hashCode() {
    return 31 * super.hashCode() + color.hashCode();
  }

  @Override
  public String toString() {
    return "(" + x + "," + y + "," + color + ")";
  }
}
```

### task11/StrictPoint.java (задание 11)

```java
package task11;

// исправление 1: strict class equality — сравниваем только объекты своего класса
public class StrictPoint {
  final int x;
  final int y;

  StrictPoint(int x, int y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    StrictPoint other = (StrictPoint) o;
    return x == other.x && y == other.y;
  }

  @Override
  public int hashCode() {
    return 31 * x + y;
  }

  @Override
  public String toString() {
    return "(" + x + "," + y + ")";
  }
}
```

### task11/StrictColoredPoint.java (задание 11)

```java
package task11;

// подтип с цветом: getClass() в equals родителя даёт симметрию false/false
public class StrictColoredPoint extends StrictPoint {
  final String color;

  StrictColoredPoint(int x, int y, String color) {
    super(x, y);
    this.color = color;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    StrictColoredPoint other = (StrictColoredPoint) o;
    return x == other.x && y == other.y && color.equals(other.color);
  }

  @Override
  public int hashCode() {
    return 31 * super.hashCode() + color.hashCode();
  }

  @Override
  public String toString() {
    return "(" + x + "," + y + "," + color + ")";
  }
}
```

### task11/PlainPoint.java (задание 11)

```java
package task11;

// исправление 2, часть 1: простой value без цвета
public record PlainPoint(int x, int y) {
}
```

### task11/Colored.java (задание 11)

```java
package task11;

// исправление 2, часть 2: цвет — композиция, а не наследование;
// record сам даёт симметричный equals/hashCode
public record Colored<T>(T value, String color) {
}
```

### task11/Task11Demo.java (задание 11)

```java
package task11;

import java.util.HashSet;
import java.util.Set;

// нарушение симметричности + два исправления
public class Task11Demo {
  public static void main(String[] args) {
    // == нарушение симметричности ==
    Point p = new Point(1, 2);
    ColoredPoint cp = new ColoredPoint(1, 2, "red");

    System.out.println("p.equals(cp)   = " + p.equals(cp));   // true: цвет не важен Point'у
    System.out.println("cp.equals(p)   = " + cp.equals(p));   // false: p не ColoredPoint

    // HashSet теряет элементы из-за несимметричного equals/hashCode
    Set<Point> set = new HashSet<>();
    set.add(p);
    set.add(cp);
    System.out.println("set size = " + set.size()); // 2, хотя для Point'а они равны

    // == исправление 1: strict class equality ==
    StrictPoint sp = new StrictPoint(1, 2);
    StrictColoredPoint scp = new StrictColoredPoint(1, 2, "red");
    System.out.println("strict: sp.equals(scp) = " + sp.equals(scp));  // false
    System.out.println("strict: scp.equals(sp) = " + scp.equals(sp));  // false: симметрия

    // == исправление 2: композиция цвета ==
    Colored<PlainPoint> left = new Colored<>(new PlainPoint(1, 2), "red");
    Colored<PlainPoint> same = new Colored<>(new PlainPoint(1, 2), "red");
    Colored<PlainPoint> other = new Colored<>(new PlainPoint(1, 2), "blue");
    System.out.println("compose: left.equals(same) = " + left.equals(same)); // true
    System.out.println("compose: left.equals(other) = " + left.equals(other)); // false
  }
}
```

---

## Задание 12. Sealed-иерархия результата

### task12/EnrollmentResult.java (задания 12, 15)

```java
package task12;

// sealed-иерархия: компилятор знает все варианты результата
public sealed interface EnrollmentResult
    permits Accepted, Rejected, WaitListed, Deferred {
}
```

### task12/Accepted.java (задания 12, 15)

```java
package task12;

// принято
public record Accepted(long studentId, long courseId) implements EnrollmentResult {
}
```

### task12/Rejected.java (задания 12, 15)

```java
package task12;

// отказано с причиной
public record Rejected(long studentId, long courseId, String reason) implements EnrollmentResult {
}
```

### task12/WaitListed.java (задания 12, 15)

```java
package task12;

// в лист ожидания
public record WaitListed(long studentId, long courseId, int position) implements EnrollmentResult {
}
```

### task12/Deferred.java (задания 12, 15)

```java
package task12;

// отложено: добавлен позже, компилятор потребовал новую ветку в switch (см. answer.md)
public record Deferred(long studentId, long courseId, String semester) implements EnrollmentResult {
}
```

### task12/Task12Demo.java (задание 12)

```java
package task12;

// exhaustive switch без default: новый permitted-тип ломает компиляцию до добавления ветки
public class Task12Demo {
  static String describe(EnrollmentResult result) {
    return switch (result) {
      case Accepted a -> "accepted: student " + a.studentId();
      case Rejected r -> "rejected: " + r.reason();
      case WaitListed w -> "waitlisted #" + w.position();
      case Deferred d -> "deferred to " + d.semester();
    };
  }

  public static void main(String[] args) {
    System.out.println(describe(new Accepted(1, 10)));
    System.out.println(describe(new Rejected(2, 10, "course full")));
    System.out.println(describe(new WaitListed(3, 10, 5)));
    System.out.println(describe(new Deferred(4, 10, "spring")));
  }
}
```

---

## Задание 13. Удаление type code

### task13/TypeCodeBefore.java (задание 13)

```java
package task13;

import src.Course;

// до рефакторинга: type code + switch на каждое новое значение
public class TypeCodeBefore {
  // каждое новое значение "notificationType" требует правки этого метода
  static String announce(String notificationType, Course course) {
    switch (notificationType) {
      case "email":
        return "email: курс " + course.title();
      case "sms":
        return "sms: курс " + course.title();
      case "push":
        return "push: курс " + course.title();
      default:
        throw new IllegalArgumentException("unknown type: " + notificationType);
    }
  }

  public static void main(String[] args) {
    Course course = new Course(1, "Java", 10);
    System.out.println(announce("email", course));
    System.out.println(announce("sms", course));
  }
}
```

### task13/NotificationChannel.java (задание 13)

```java
package task13;

import src.Course;

// различия поведения уехали из switch в реализации интерфейса
public interface NotificationChannel {
  void announce(Course course);
}
```

### task13/EmailChannel.java (задание 13)

```java
package task13;

import src.Course;

public class EmailChannel implements NotificationChannel {
  @Override
  public void announce(Course course) {
    System.out.println("email: курс " + course.title());
  }
}
```

### task13/SmsChannel.java (задание 13)

```java
package task13;

import src.Course;

public class SmsChannel implements NotificationChannel {
  @Override
  public void announce(Course course) {
    System.out.println("sms: курс " + course.title());
  }
}
```

### task13/PushChannel.java (задание 13)

```java
package task13;

import src.Course;

public class PushChannel implements NotificationChannel {
  @Override
  public void announce(Course course) {
    System.out.println("push: курс " + course.title());
  }
}
```

### task13/Notifier.java (задание 13)

```java
package task13;

import src.Course;

// клиент не знает о конкретных каналах и о type code
public class Notifier {
  private final NotificationChannel channel;

  public Notifier(NotificationChannel channel) {
    this.channel = channel;
  }

  public void announce(Course course) {
    channel.announce(course);
  }
}
```

### task13/Task13Demo.java (задание 13)

```java
package task13;

import src.Course;

// новый канал = новый класс, существующий код не трогаем
public class Task13Demo {
  public static void main(String[] args) {
    Course course = new Course(1, "Java", 10);

    new Notifier(new EmailChannel()).announce(course);
    new Notifier(new SmsChannel()).announce(course);
    new Notifier(new PushChannel()).announce(course);
  }
}
```

---

## Задание 14. Наследование взрывает комбинации

### task14/CourseFormat.java (задание 14)

```java
package task14;

// ось формата курса
public interface CourseFormat {
  String describe();
}
```

### task14/Online.java (задание 14)

```java
package task14;

public class Online implements CourseFormat {
  @Override
  public String describe() {
    return "online";
  }
}
```

### task14/Classroom.java (задание 14)

```java
package task14;

public class Classroom implements CourseFormat {
  @Override
  public String describe() {
    return "classroom";
  }
}
```

### task14/Hybrid.java (задание 14)

```java
package task14;

public class Hybrid implements CourseFormat {
  @Override
  public String describe() {
    return "hybrid";
  }
}
```

### task14/PaymentPolicy.java (задание 14)

```java
package task14;

// ось оплаты: независима от формата
public interface PaymentPolicy {
  int price(int basePrice);
}
```

### task14/Free.java (задание 14)

```java
package task14;

public class Free implements PaymentPolicy {
  @Override
  public int price(int basePrice) {
    return 0;
  }
}
```

### task14/Fixed.java (задание 14)

```java
package task14;

// фиксированная цена независимо от base
public class Fixed implements PaymentPolicy {
  private final int fixedPrice;

  public Fixed(int fixedPrice) {
    this.fixedPrice = fixedPrice;
  }

  @Override
  public int price(int basePrice) {
    return fixedPrice;
  }
}
```

### task14/Subscription.java (задание 14)

```java
package task14;

// подписка: базовая цена делится на 12 месяцев
public class Subscription implements PaymentPolicy {
  @Override
  public int price(int basePrice) {
    return basePrice / 12;
  }
}
```

### task14/SelfPaced.java (задание 14)

```java
package task14;

// новый формат: только новый класс, остальной код не тронут
public class SelfPaced implements CourseFormat {
  @Override
  public String describe() {
    return "self-paced";
  }
}
```

### task14/Installment.java (задание 14)

```java
package task14;

// новая оплата: только новый класс, остальной код не тронут
public class Installment implements PaymentPolicy {
  private final int parts;

  public Installment(int parts) {
    if (parts <= 0) throw new IllegalArgumentException("parts must be > 0");
    this.parts = parts;
  }

  @Override
  public int price(int basePrice) {
    return basePrice / parts;
  }
}
```

### task14/CourseOffer.java (задание 14)

```java
package task14;

// вместо девяти подклассов (Online+Free, Online+Fixed, ...) — композиция осей
public class CourseOffer {
  private final CourseFormat format;
  private final PaymentPolicy payment;

  public CourseOffer(CourseFormat format, PaymentPolicy payment) {
    this.format = format;
    this.payment = payment;
  }

  public String describe(int basePrice) {
    return format.describe() + " / " + payment.price(basePrice) + " RUB";
  }
}
```

### task14/Task14Demo.java (задание 14)

```java
package task14;

// комбинации собираются из компонентов: 3x3 = 9 вариантов без девяти подклассов
public class Task14Demo {
  public static void main(String[] args) {
    CourseOffer[] offers = {
        new CourseOffer(new Online(), new Free()),
        new CourseOffer(new Online(), new Fixed(5000)),
        new CourseOffer(new Online(), new Subscription()),
        new CourseOffer(new Classroom(), new Free()),
        new CourseOffer(new Classroom(), new Fixed(5000)),
        new CourseOffer(new Classroom(), new Subscription()),
        new CourseOffer(new Hybrid(), new Free()),
        new CourseOffer(new Hybrid(), new Fixed(5000)),
        new CourseOffer(new Hybrid(), new Subscription())
    };

    for (CourseOffer offer : offers) {
      System.out.println(offer.describe(12000));
    }

    // добавление новой оси: без правок CourseOffer и без новых комбинированных классов
    System.out.println(new CourseOffer(new SelfPaced(), new Installment(4)).describe(12000));
  }
}
```

---

## Задание 15. Расширяемый движок правил

В качестве типизированного результата использует sealed-типы `task12.EnrollmentResult`, `Accepted`, `Rejected`, `WaitListed`, `Deferred` из задания 12 (листинги выше).

### task15/Rule.java (задание 15)

```java
package task15;

import src.Course;
import src.Student;
import task12.EnrollmentResult;

// открытая стратегия правила: типизированный результат — sealed EnrollmentResult из task12
public interface Rule {
  EnrollmentResult check(Student student, Course course);
}
```

### task15/CourseOpenRule.java (задание 15)

```java
package task15;

import src.Course;
import src.Student;
import task12.Accepted;
import task12.EnrollmentResult;
import task12.Rejected;

// правило: курс открыт
public class CourseOpenRule implements Rule {
  @Override
  public EnrollmentResult check(Student student, Course course) {
    if (course.status() != Course.Status.OPEN) {
      return new Rejected(student.id(), course.id(), "course closed");
    }
    return new Accepted(student.id(), course.id());
  }
}
```

### task15/CapacityRule.java (задание 15)

```java
package task15;

import src.Course;
import src.Student;
import task12.Accepted;
import task12.EnrollmentResult;
import task12.Rejected;

// правило: есть свободные места
public class CapacityRule implements Rule {
  @Override
  public EnrollmentResult check(Student student, Course course) {
    if (course.seatsLeft() <= 0) {
      return new Rejected(student.id(), course.id(), "no seats left");
    }
    return new Accepted(student.id(), course.id());
  }
}
```

### task15/NotEnrolledRule.java (задание 15)

```java
package task15;

import src.Course;
import src.Student;
import task12.Accepted;
import task12.EnrollmentResult;
import task12.Rejected;

// правило: студент ещё не записан на этот курс
public class NotEnrolledRule implements Rule {
  @Override
  public EnrollmentResult check(Student student, Course course) {
    if (course.enrolledStudents().contains(student.id())) {
      return new Rejected(student.id(), course.id(), "already enrolled");
    }
    return new Accepted(student.id(), course.id());
  }
}
```

### task15/PrerequisiteRule.java (задание 15)

```java
package task15;

import src.Course;
import src.Student;
import task12.Accepted;
import task12.EnrollmentResult;
import task12.Rejected;

// правило: пререквизиты курса
public class PrerequisiteRule implements Rule {
  @Override
  public EnrollmentResult check(Student student, Course course) {
    if (course.hasPrerequisite() && !student.hasPrerequisite()) {
      return new Rejected(student.id(), course.id(), "prerequisite missing");
    }
    return new Accepted(student.id(), course.id());
  }
}
```

### task15/AgeRule.java (задание 15)

```java
package task15;

import src.Course;
import src.Student;
import task12.Accepted;
import task12.EnrollmentResult;
import task12.Rejected;

// правило: минимальный возраст
public class AgeRule implements Rule {
  private final int minAge;

  public AgeRule(int minAge) {
    this.minAge = minAge;
  }

  @Override
  public EnrollmentResult check(Student student, Course course) {
    if (student.age() < minAge) {
      return new Rejected(student.id(), course.id(), "too young, min age " + minAge);
    }
    return new Accepted(student.id(), course.id());
  }
}
```

### task15/AllOf.java (задание 15)

```java
package task15;

import src.Course;
import src.Student;
import task12.Accepted;
import task12.EnrollmentResult;

// композиция правил: все должны пройти, возвращается первое отклонение
public class AllOf implements Rule {
  private final Rule[] rules;

  public AllOf(Rule... rules) {
    this.rules = rules;
  }

  @Override
  public EnrollmentResult check(Student student, Course course) {
    EnrollmentResult lastAccepted = new Accepted(student.id(), course.id());
    for (Rule rule : rules) {
      EnrollmentResult result = rule.check(student, course);
      // проверяем тип результата, а не класс правила
      if (result instanceof Accepted accepted) {
        lastAccepted = accepted;
      } else {
        return result;
      }
    }
    return lastAccepted;
  }
}
```

### task15/AnyOf.java (задание 15)

```java
package task15;

import src.Course;
import src.Student;
import task12.Accepted;
import task12.EnrollmentResult;
import task12.Rejected;

// композиция правил: достаточно пройти любое, иначе первое отклонение
public class AnyOf implements Rule {
  private final Rule[] rules;

  public AnyOf(Rule... rules) {
    this.rules = rules;
  }

  @Override
  public EnrollmentResult check(Student student, Course course) {
    EnrollmentResult firstRejected = null;
    for (Rule rule : rules) {
      EnrollmentResult result = rule.check(student, course);
      if (result instanceof Accepted) {
        return result;
      }
      if (firstRejected == null) {
        firstRejected = result;
      }
    }
    return firstRejected != null
        ? firstRejected
        : new Rejected(student.id(), course.id(), "no rules configured");
  }
}
```

### task15/EnrollmentEngine.java (задание 15)

```java
package task15;

import src.Course;
import src.Student;
import task12.EnrollmentResult;

// сервис зависит только от контракта Rule: ни одного instanceof по классам правил
public class EnrollmentEngine {
  private final Rule rule;

  public EnrollmentEngine(Rule rule) {
    this.rule = rule;
  }

  public EnrollmentResult evaluate(Student student, Course course) {
    return rule.check(student, course);
  }
}
```

### task15/Task15Demo.java (задание 15)

```java
package task15;

import src.Course;
import src.Student;
import task12.Accepted;
import task12.Deferred;
import task12.EnrollmentResult;
import task12.Rejected;
import task12.WaitListed;

// dynamic dispatch: rule.check(...) выполняется на классе каждого правила,
// engine знать эти классы не нужно
public class Task15Demo {
  static void print(EnrollmentResult result) {
    System.out.println(switch (result) {
      case Accepted a -> "accepted: student " + a.studentId();
      case Rejected r -> "rejected: " + r.reason();
      case WaitListed w -> "waitlisted #" + w.position();
      case Deferred d -> "deferred to " + d.semester();
    });
  }

  public static void main(String[] args) {
    Course java = new Course(1, "Java", 10);
    java.setHasPrerequisite(true);
    java.setCapacity(2);

    // пять открытых стратегий, композиция allOf
    Rule defaultPolicy = new AllOf(
        new CourseOpenRule(),
        new CapacityRule(),
        new NotEnrolledRule(),
        new PrerequisiteRule(),
        new AgeRule(16));

    EnrollmentEngine engine = new EnrollmentEngine(defaultPolicy);

    Student anya = new Student(1, "Аня", 20, true);
    Student borya = new Student(2, "Боря", 12, true);
    Student vera = new Student(3, "Вера", 25, false);

    print(engine.evaluate(anya, java)); // accepted
    java.addEnrolled(anya.id());
    print(engine.evaluate(anya, java)); // rejected: already enrolled
    print(engine.evaluate(borya, java)); // rejected: too young
    print(engine.evaluate(vera, java)); // rejected: prerequisite missing

    java.setStatus(Course.Status.CLOSED);
    print(engine.evaluate(borya, java)); // rejected: course closed (другое правило)

    // новая политика — новая стратегия, engine не меняется
    java.setStatus(Course.Status.OPEN);
    Rule anyOfAgeOrOpen = new AnyOf(new AgeRule(16), new CourseOpenRule());
    System.out.println("anyOf:");
    print(new EnrollmentEngine(anyOfAgeOrOpen).evaluate(borya, java));
  }
}
```

---
