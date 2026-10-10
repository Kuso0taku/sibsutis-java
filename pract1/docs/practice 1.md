# ПРАКТИЧЕСКАЯ РАБОТА № 1

## Полиморфизм, абстракция и контракты подтипов

| | |
|---|---|
| Университет | ФГБОУ ВО «Сибирский государственный университет телекоммуникаций и информатики» (СибГУТИ) |
| Институт | Институт вычислительной техники (ИВТ) |
| Кафедра | Кафедра вычислительных систем (ВС) |
| Дисциплина | Основы разработки на Java |
| Студент | Устюжанин Денис Константинович |
| Группа | ИС-541 |
| Преподаватель | Нигматулин Руслан Раульевич |
| Город, дата | Новосибирск, 10.10.2026 |

---

## Введение

### Цель работы

Освоить объектно-ориентированные механизмы Java, лежащие в основе расширяемых
систем: интерфейсы и динамическую диспетчеризацию, абстрактные классы и шаблонный
метод, переопределение и перегрузку, принцип подстановки Лисков, паттерны
«стратегия» и «декоратор», закрытые и открытые иерархии (`sealed`), а также
типичные ошибки проектирования (`type code`, комбинаторный взрыв наследования,
нарушение контракта `equals`). Каждое задание оформлено как отдельный
демонстрационный пример с запускаемым методом `main`.

### Общая предметная модель

Все задания используют сквозную модель учебного центра **CourseHub**. Общие,
неизменяемые между заданиями типы вынесены в пакет `src` — это «общая модель»,
которую задачи переиспользуют через `import src.*`, а не копируют.

**`src/Course.java`** — курс: идентификатор, название, длительность, статус,
вместимость, множество записанных студентов.

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

**`src/Student.java`** — студент: идентификатор, имя, возраст, наличие
пререквизита.

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

### Структура проекта и сборка

```
pract1/
├── pom.xml              # Maven: source/target 21, компиляция всего проекта
├── tasks.md             # формулировки заданий
├── src/                 # общая неизменяемая модель (package src)
│   ├── Course.java
│   └── Student.java
├── task1/ … task15/     # по одному пакету на задание (package taskN)
└── target/classes       # результат компиляции
```

Каждое задание лежит в собственном пакете `taskN`, что позволяет импортировать
код предыдущих заданий (`task1.CourseFormatter`, `task12.EnrollmentResult` и т. д.)
и одновременно избегать конфликтов имён классов.

Сборка и запуск демонстрации:

```bash
mvn -f pract1/pom.xml clean compile
java -cp pract1/target/classes task1.Task1Demo
```

Два файла (`task6/BrokenOverride.java`, `task10/BrokenDefaults.java`) намеренно **не
компилируются** — они исключены из сборки в `pom.xml` и служат материалом для
разбора ошибок:

```xml
<excludes>
    <exclude>**/Broken*.java</exclude>
</excludes>
```

Проверка таких файлов вручную: `javac pract1/task6/BrokenOverride.java`.

---

# Уровень I. Полиморфизм

## Задание 1. Один контракт, две реализации

### Формулировка

Объявить интерфейс `CourseFormatter` с методом `format(Course)`. Реализовать
`CompactCourseFormatter` и `DetailedCourseFormatter`. Вызвать обе реализации через
переменную интерфейсного типа. Объяснить, что известно компилятору и что
выбирается во время выполнения.

### Алгоритм решения

1. Объявить интерфейс `CourseFormatter` с единственным методом
   `String format(Course course)`.
2. Создать две реализации интерфейса и пометить методы `@Override`.
3. В клиентском классе объявить переменные интерфейсного типа и присвоить им
   объекты конкретных классов.
4. Вызвать `format()` через обе переменные и сравнить результат.
5. Сформулировать различие между знанием компилятора (compile-time) и выбором
   реализации (runtime).

### Разбор концепций

**Интерфейс** описывает контракт — набор методов без реализации. Переменная
интерфейсного типа хранит ссылку на объект, но компилятору доступны только те
методы, которые объявлены в интерфейсе. Компилятор **не знает** конкретного
класса объекта и потому не может встроить вызов конкретной реализации.

**Динамическая диспетчеризация (dynamic dispatch)** — выбор конкретного метода
во время выполнения по фактическому классу объекта. JVM хранит для каждого класса
таблицу методов и по ней находит нужную реализацию. Именно поэтому один и тот же
вызов `formatter.format(course)` приводит к разному коду в зависимости от того,
какой объект лежит в переменной.

За счёт этого клиент зависит только от абстракции: добавление новой реализации не
требует правки клиентского кода (принцип открытости/закрытости, инверсия
зависимостей). Аннотация `@Override` заставляет компилятор проверить, что метод
действительно переопределяет контракт, и защищает от случайной перегрузки
(подробно — в задании 6).

### Ключевые листинги

Интерфейс — общая точка расширения:

```java
package task1;

import src.Course;

// один контракт, две реализации
public interface CourseFormatter {
  String format(Course course);
}
```

Вызов обеих реализаций через переменную интерфейсного типа:

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

### Результат

| Вопрос | Ответ |
|---|---|
| Что известно компилятору? | Только то, что у объекта есть метод `format(Course)`. Обе переменные имеют статический тип `CourseFormatter`. |
| Что выбирается во время выполнения? | Конкретная реализация (`Compact` или `Detailed`) — по классу объекта. |
| Что видит программист при чтении кода? | Намерение вызвать контракт, не привязываясь к реализации. |

---

## Задание 2. Dynamic dispatch на бумаге

### Формулировка

Для иерархии `NotificationSender` → `EmailSender`, `ConsoleSender` предсказать
результат вызовов через ссылки разных compile-time типов. Добавить перегруженный
метод и отделить overload resolution от override dispatch.

### Алгоритм решения

1. Определить интерфейс `NotificationSender` и две реализации.
2. Создать ссылки интерфейсного и конкретного типов на один и тот же объект.
3. Объявить перегруженные методы `describe(NotificationSender)` и
   `describe(EmailSender)`.
4. Вызвать `send()` и `describe()` и зафиксировать предсказанный результат до
   запуска, затем сверить с фактическим.
5. Разделить понятия overload resolution (compile-time) и override dispatch
   (runtime).

### Разбор концепций

**Overload resolution** (выбор перегрузки) выполняет компилятор по **статическим**
типам аргументов. Перегрузка никак не связана с классом объекта: если параметр
объявлен как `NotificationSender`, будет выбрана перегрузка с этим параметром,
даже если фактический объект — `EmailSender`.

**Override dispatch** (выбор переопределения) выполняет JVM по **фактическому**
классу объекта. Поэтому `send()` при одном и том же статическом типе вызывает
разный код.

Оба механизма независимы, и их важно не путать: в одном выражении они могут
сработать по-разному. Это классический источник ошибок, когда программист ожидает
выбора по объекту, а получает выбор по типу ссылки.

### Ключевые листинги

```java
package task2;

// контракт отправки уведомления
public interface NotificationSender {
  void send(String recipient, String message);
}
```

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

### Таблица предсказаний

| Вызов | Compile-time тип | Runtime класс | Что выполнится |
|---|---|---|---|
| `asInterface.send(...)` | `NotificationSender` | `EmailSender` | `EmailSender.send` — override dispatch |
| `console.send(...)` | `NotificationSender` | `ConsoleSender` | `ConsoleSender.send` |
| `describe(asInterface)` | `NotificationSender` | `EmailSender` | `describe(NotificationSender)` |
| `describe(asConcrete)` | `EmailSender` | `EmailSender` | `describe(EmailSender)` |

---

## Задание 3. Полиморфный массив

### Формулировка

Сохранить несколько `CourseFormatter` в массиве и применить каждый к одному курсу
без `if` по типу. Добавить третью реализацию, не меняя обход. Критерий: клиент
зависит только от интерфейса.

### Алгоритм решения

1. Переиспользовать интерфейс `CourseFormatter` из задания 1 (`import task1.*`).
2. Создать третью реализацию `HtmlCourseFormatter`.
3. Собрать массив элементов интерфейсного типа.
4. Обойти массив одним циклом и вызвать `format()` у каждого элемента.
5. Убедиться, что добавление реализации не изменило цикл обхода.

### Разбор концепций

Массив интерфейсного типа хранит ссылки на объекты разных классов. Один цикл
обрабатывает их единообразно — это **полиморфный обход**. Отсутствие проверок
`if` по типу означает, что выбор поведения полностью делегирован объекту:
клиент не знает и не должен знать о конкретных реализациях.

Тот факт, что третья реализация добавляется без правки цикла, доказывает: контракт
является общей точкой расширения. Это практическое проявление принципа
открытости/закрытости.

### Ключевые листинги

Третья реализация — только новый класс, существующие не трогаются:

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

Единый полиморфный обход:

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

# Уровень II. Абстракция и наследование

## Задание 4. Абстрактный базовый класс

### Формулировка

Создать `AbstractNotificationSender` с общей проверкой адресата и абстрактным
`doSend`. Реализовать email- и console-варианты. Объяснить, почему публичный
алгоритм можно сделать `final` и где остаётся точка расширения.

### Алгоритм решения

1. Объявить абстрактный класс, реализующий `NotificationSender`.
2. Вынести общую проверку адресата в публичный метод `send()` и пометить его
   `final`.
3. Объявить абстрактный защищённый метод `doSend()`.
4. Реализовать `doSend()` в подклассах `EmailNotificationSender` и
   `ConsoleNotificationSender`.
5. Проверить, что невалидный адресат не доходит до `doSend()`.

### Разбор концепций

Использован приём **«шаблонный метод» (template method)**: базовый класс задаёт
скелет алгоритма, а отдельные шаги оставляет абстрактными. Здесь скелет — это
«проверить адресат → выполнить отправку», а изменяемый шаг — сама отправка.

Метод `send()` объявлен `final`, чтобы подклассы не могли обойти или нарушить общий
инвариант (проверку адресата). Инвариант должен быть неизменяемым, иначе разные
подклассы начнут трактовать его по-своему, и контракт развалится.

Единственная точка расширения — `doSend()`. Это узкий и явный контракт, тогда как
публичный алгоритм зафиксирован. Абстрактный класс выбран потому, что есть **общее
поведение** (проверка) и **общий алгоритм**; интерфейс такого позволить не может —
в нём нельзя зафиксировать тело метода.

### Ключевые листинги

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

### Формулировка

Для понятий `Identifiable`, `CoursePolicy`, `BaseEntity`, `Formatter`,
`ClockProvider` выбрать `interface`, `abstract class`, обычный класс или
отсутствие отдельного типа. Для каждого решения записать мотивировку и цену
изменения.

### Алгоритм решения

1. Проанализировать каждое понятие: есть ли общее состояние, общий алгоритм или
   только контракт поведения.
2. Выбрать вид типа по правилу: контракт — интерфейс; общее состояние/алгоритм —
   абстрактный класс; только данные — record/class; достаточно лямбды — отдельный
   тип не нужен.
3. Для каждого решения зафиксировать мотивировку и цену будущего изменения.
4. Оформить результат в виде таблицы.

### Разбор концепций и решение

| Понятие | Выбор | Мотивировка | Цена изменения |
|---|---|---|---|
| `Identifiable` | `interface` | Это не «тип с состоянием», а обещание `id()`. Подписаться может кто угодно: сущность, record, даже enum. | Малая: новый реализатор не ломает существующий код. |
| `CoursePolicy` | `interface` | Стратегия без состояния: только метод решения. Легко комбинировать (`allOf`, `anyOf`) и подменять. | Малая: новая политика — новый класс, `EnrollmentService` не меняется. |
| `BaseEntity` | `abstract class` | Есть общее изменяемое состояние и поведение (id, `equals`/`hashCode`). Нужен базовый конструктор — это уже наследование состояния. | Высокая: Java не даёт наследовать ещё у кого-то; все сущности привязаны к иерархии. |
| `Formatter` | `interface` | Форматирование — чистое поведение без общего состояния. | Малая: новая реализация не трогает существующие. |
| `ClockProvider` | `interface` (функциональный) | Нужна подмена времени в тестах: `() -> fixedInstant`. Абстрактный класс избыточен — нет общего кода. | Минимальная: лямбда/метод-ссылка. |

**Правило выбора:**

- есть общее состояние или общий алгоритм, который нельзя дублировать →
  `abstract class` (как `AbstractNotificationSender` в задании 4);
- есть только контракт поведения → `interface` (задания 1, 9);
- есть только данные, поведение не меняется → обычный класс / record;
- отдельный тип не нужен, когда хватает лямбды (`ClockProvider`).

### Иллюстрация форм типов

```java
interface Identifiable { long id(); }
interface Formatter<T> { String format(T value); }

@FunctionalInterface
interface ClockProvider { java.time.Instant now(); }

abstract class BaseEntity {
  private long id;            // общее состояние
  public long id() { return id; }
  @Override public boolean equals(Object o) { /* общий код */ return false; }
  @Override public int hashCode() { /* общий код */ return 0; }
}

interface CoursePolicy {     // только контракт, без состояния
  boolean allows(src.Student student, src.Course course);
}
```

---

## Задание 6. Переопределение без случайной перегрузки

### Формулировка

Исправить набор методов с ошибками в параметрах, доступе и возвращаемых типах.
Везде, где ожидается переопределение, использовать `@Override`. Продемонстрировать
случай, когда код компилировался без аннотации, но вызывался не тот метод.

### Алгоритм решения

1. Собрать примеры трёх ошибок: другой параметр, сужение доступа, несовместимый
   возвращаемый тип.
2. Показать, что `@Override` превращает эти ошибки в ошибки компиляции.
3. Исправить методы: совпадающие сигнатуры, доступ не сужается, возвращаемый тип
   ковариантный.
4. Продемонстрировать «тихий» дефект: без `@Override` метод стал перегрузкой, и
   через базовый тип вызывается не тот метод.

### Разбор концепций

Переопределение возможно только при **совпадении сигнатуры** и **несужающемся
доступе**. Возвращаемый тип может быть **ковариантным** — то есть подтипом
исходного (`Integer` вместо `Number`).

Разный параметр превращает метод в **перегрузку**, а не в переопределение. Без
`@Override` компилятор молчит, и вызов через базовый тип уходит в родительскую
версию — это скрытый дефект, который проявляется только в рантайме.

`@Override` обязателен для самопроверки: он делает намерение явным и заставляет
компилятор проверять, что метод действительно переопределяет контракт.

### Ключевые листинги

Намеренно ошибочный код (не компилируется):

```java
package task6;

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

Компилятор сообщает четыре ошибки:

```text
BrokenOverride.java:15: error: send(int) in Broken does not override or implement a method from a supertype
BrokenOverride.java:20: error: log(String) in Broken cannot override log(String) in Base
        attempting to assign weaker access privileges; was package
BrokenOverride.java:24: error: value() in Broken cannot override value() in Base
        return type String is not compatible with Number
```

Исправленный вариант:

```java
package task6;

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
```

«Тихий» дефект без `@Override` — перегрузка вместо переопределения:

```java
package task6;

import task2.EmailSender;
import task2.NotificationSender;

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

Вывод показывает дефект — первая строка уходит в базовую версию:

```text
base.handle(NotificationSender)
sub.handle(EmailSender)
```

---

## Задание 7. Композиция вместо наследования

### Формулировка

Дан `LoggingEmailSender extends EmailSender`, затем требуется retry и metrics.
Перепроектировать logging и metrics как декораторы вокруг `NotificationSender`.
Показать два порядка композиции и объяснить разницу наблюдаемого поведения.

### Алгоритм решения

1. Зафиксировать недостаток исходного решения: комбинации подклассов (logging,
   retry, metrics) растут лавинообразно.
2. Объявить декоратор `LoggingDecorator`, реализующий `NotificationSender` и
   оборачивающий другого отправителя.
3. Аналогично объявить `MetricsDecorator`.
4. Собрать цепочки в двух порядках и сравнить вывод.
5. Сформулировать правило: внешний декоратор первым видит вызов.

### Разбор концепций

**Декоратор** добавляет поведение, не меняя класс: он реализует тот же интерфейс и
хранит ссылку на оборачиваемый объект. Композиция заменяет наследование: вместо N
комбинаций подклассов достаточно N независимых декораторов (сравните с
комбинаторным взрывом в задании 14).

Порядок обёрток меняет поведение. В цепочке `metrics(logging)` счётчик срабатывает
раньше лога; в `logging(metrics)` лог окружает счётчик. Это важно документировать,
потому что наблюдаемое поведение зависит от порядка, а не только от состава.

### Ключевые листинги

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

Два порядка композиции:

```java
package task7;

import task2.EmailSender;
import task2.NotificationSender;

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

### Сравнение поведения

| Порядок | Наблюдаемое поведение |
|---|---|
| `Metrics(Logging(Email))` | метрика считает попытку до лога делегирования; лог видит только письма |
| `Logging(Metrics(Email))` | сначала «start», затем счётчик метрик, затем письмо, затем «end» |

Разница видна в выводе: лог-обёртка окружает счётчик или находится внутри него.
Правило: **внешний декоратор — первый, кто видит вызов**.

---

# Уровень III. Контракты подтипов

## Задание 8. Нарушение LSP

### Формулировка

Найти проблему в `ReadOnlyCourseRepository extends CourseRepository`, который
выбрасывает исключение из `save`. Перестроить интерфейсы так, чтобы read-only
реализация не обещала невозможную операцию.

### Алгоритм решения

1. Описать исходный дефект: подтип обещает `save()`, но бросает
   `UnsupportedOperationException`.
2. Разделить контракт на `CourseReadRepository` и `CourseWriteRepository`.
3. Объявить `CourseRepository` как объединение чтения и записи.
4. Реализовать read-only вариант только через интерфейс чтения.
5. Показать, что вызов `save()` у read-only теперь не компилируется.

### Разбор концепций

**Принцип подстановки Барбары Лисков (LSP)**: объект подтипа должен использоваться
везде, где ожидается базовый тип, без изменения корректности программы. Если
подтип не может выполнить обещанную операцию, **контракт базового типа выбран
неверно**. Проблема решается не заглушкой, а разделением интерфейсов (**принцип
разделения интерфейсов, ISP**).

Возможность физически не иметь метода `save()` переносит ошибку из рантайма в
compile-time — это надёжнее: неверный вызов просто не скомпилируется.

### Проблема (было)

```java
interface CourseRepository {
  Optional<Course> findById(long id);
  void save(Course course);
}

class ReadOnlyCourseRepository extends CourseRepository {
  public void save(Course course) {
    throw new UnsupportedOperationException("read-only");
  }
}
```

Подтип обещал `save`, но бросал исключение. Любой код, работающий с
`CourseRepository`, получал скрытую бомбу: вызов компилировался, но падал в
рантайме. Замена `InMemoryCourseRepository` на `ReadOnlyCourseRepository` ломала
поведение — нарушение подстановки.

### Исправление

```java
package task8;

import src.Course;
import java.util.Optional;

// только чтение: контракт не обещает запись
public interface CourseReadRepository {
  Optional<Course> findById(long id);
}
```

```java
package task8;

import src.Course;

// только запись: отдельный контракт
public interface CourseWriteRepository {
  void save(Course course);
}
```

```java
package task8;

// полный контракт = чтение + запись
public interface CourseRepository extends CourseReadRepository, CourseWriteRepository {
}
```

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

### Формулировка

Объявить `EnrollmentPolicy` и реализации по возрасту, prerequisites и
вместимости. Клиент принимает политику конструктором. Добавление новой политики
не меняет `EnrollmentService`.

### Алгоритм решения

1. Объявить интерфейс `EnrollmentPolicy` с методом `allows(Student, Course)`.
2. Реализовать `AgePolicy`, `PrerequisitePolicy` и `CapacityPolicy`.
3. Передать политику в `EnrollmentService` через конструктор.
4. Собрать комбинированную политику и проверить разные случаи.
5. Добавить новую политику (лямбдой) без правки сервиса.

### Разбор концепций

Использован паттерн **«стратегия»**: алгоритм вынесен в отдельный объект и
подставляется извне. Сервис не содержит условий по типу политики.

**Внедрение зависимости через конструктор** (constructor injection) делает
зависимость явной и упрощает тестирование. Интерфейс функциональный, поэтому новую
политику можно задать лямбдой, не создавая отдельный класс — минимальная цена
расширения.

### Ключевые листинги

```java
package task9;

import src.Course;
import src.Student;

// открытая стратегия: новая политика — новый класс, сервис не меняется
public interface EnrollmentPolicy {
  boolean allows(Student student, Course course);
}
```

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

Комбинация политик и добавление новой без правки сервиса:

```java
package task9;

import src.Course;
import src.Student;

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

### Формулировка

Создать два интерфейса с одинаковым default-методом. Получить ошибку компиляции,
затем разрешить конфликт явным override. Объяснить, почему множественное
наследование поведения требует явного выбора.

### Алгоритм решения

1. Объявить два интерфейса с одинаковым default-методом `name()`.
2. Реализовать оба интерфейса в одном классе и получить ошибку компиляции.
3. Разрешить конфликт явным переопределением метода в классе.
4. Показать, что после override компилятор использует реализацию класса.

### Разбор концепций

Если класс наследует два несвязанных default-метода с одинаковой сигнатурой,
компилятор не может выбрать реализацию и требует явного разрешения. Явный override
в классе снимает неоднозначность и защищает от скрытого изменения поведения при
подключении нового интерфейса.

Java разрешает множественное наследование поведения именно потому, что у
интерфейсов нет состояния; при конфликте выбор обязан сделать программист.

### Ключевые листинги

Конфликт (не компилируется):

```java
package task10;

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

Сообщение компилятора:

```text
error: types BrokenNotifier and BrokenLogger are incompatible;
class BrokenNotifierLogger inherits unrelated defaults for name() from types
BrokenNotifier and BrokenLogger
```

Разрешение:

```java
package task10;

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

---

## Задание 11. Равенство в иерархии

### Формулировка

Воспроизвести нарушение симметричности между `Point` и `ColoredPoint`. Предложить
два исправления: strict class equality и композиция цвета. Обосновать, почему
final value class проще расширяемой equality-иерархии.

### Алгоритм решения

1. Реализовать `Point` с `equals` по координатам и `ColoredPoint` с добавлением
   цвета.
2. Показать несимметричность: `p.equals(cp)` истинно, `cp.equals(p)` ложно.
3. Исправление 1: сравнивать классы через `getClass()` (strict equality).
4. Исправление 2: вынести цвет в композицию (обёртка/record).
5. Обосновать простоту финального value-класса.

### Разбор концепций

Контракт `equals` требует **рефлексивности, симметричности, транзитивности** и
согласованности с `hashCode`. Наследование легко ломает симметричность и
транзитивность.

**Strict class equality** (`getClass()`) восстанавливает симметрию, но подтип с
новым полем перестаёт быть равным базовому значению — это осознанная плата.

**Композиция** убирает проблему: цвет — отдельное поле обёртки, а record
формирует корректный `equals`/`hashCode` без иерархии.

**Финальный value-класс** (record) фиксирует равенство и не может быть испорчен
подклассом — для неизменяемых значений это надёжнее расширяемой иерархии.

### Ключевые листинги

Нарушение:

```java
package task11;

// equals сравнивает только координаты — этого мало для иерархии
public class Point {
  final int x;
  final int y;

  Point(int x, int y) { this.x = x; this.y = y; }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Point)) return false;
    Point other = (Point) o;
    return x == other.x && y == other.y;
  }

  @Override
  public int hashCode() { return 31 * x + y; }

  @Override
  public String toString() { return "(" + x + "," + y + ")"; }
}
```

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
  public int hashCode() { return 31 * super.hashCode() + color.hashCode(); }
}
```

Демонстрация и два исправления:

```java
package task11;

import java.util.HashSet;
import java.util.Set;

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

Исправление 1 — строгое сравнение классов:

```java
  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    StrictPoint other = (StrictPoint) o;
    return x == other.x && y == other.y;
  }
```

Исправление 2 — композиция через record:

```java
package task11;

// цвет — композиция, а не наследование;
// record сам даёт симметричный equals/hashCode
public record Colored<T>(T value, String color) {
}

package task11;

// простой value без цвета
public record PlainPoint(int x, int y) {
}
```

Вывод демонстрации:

```text
p.equals(cp)   = true
cp.equals(p)   = false
set size = 2
strict: sp.equals(scp) = false
strict: scp.equals(sp) = false
compose: left.equals(same) = true
compose: left.equals(other) = false
```

---

# Уровень IV. Закрытые и открытые иерархии

## Задание 12. Sealed-иерархия результата

### Формулировка

Создать `sealed interface EnrollmentResult` с `Accepted`, `Rejected` и
`WaitListed`. Обработать все варианты через exhaustive switch без `default`.
Добавить новый permitted-тип и зафиксировать, где компилятор потребовал изменение.

### Алгоритм решения

1. Объявить `sealed interface EnrollmentResult` с явным списком `permits`.
2. Описать варианты `Accepted`, `Rejected`, `WaitListed` (records).
3. Обработать результат в `switch` без ветки `default`.
4. Добавить новый permitted-тип `Deferred`.
5. Зафиксировать две точки правки: список `permits` и `switch`.

### Разбор концепций

**Sealed-иерархия** закрывает список подтипов: снаружи добавить вариант нельзя.
Это даёт компилятору полную картину.

`switch` по sealed-типу **исчерпывающий без `default`**: компилятор проверяет все
ветки. Новый вариант немедленно вызывает ошибку компиляции в местах обработки.
Список `permits` и `switch` — обязательные точки сопровождения при расширении;
забыть вариант невозможно.

### Ключевые листинги

```java
package task12;

// sealed-иерархия: компилятор знает все варианты результата
public sealed interface EnrollmentResult
    permits Accepted, Rejected, WaitListed, Deferred {
}
```

```java
package task12;

public record Accepted(long studentId, long courseId) implements EnrollmentResult { }

public record Rejected(long studentId, long courseId, String reason) implements EnrollmentResult { }

public record WaitListed(long studentId, long courseId, int position) implements EnrollmentResult { }

public record Deferred(long studentId, long courseId, String semester) implements EnrollmentResult { }
```

Исчерпывающий `switch` без `default`:

```java
package task12;

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

> **Замечание о версии языка.** Pattern matching в `switch` требует Java 21,
> поэтому в `pom.xml` установлены `maven.compiler.source/target = 21`.

### Что сделал компилятор

Изначально в `EnrollmentResult` было три permitted-типа, а `describe` перечислял
три ветки. После добавления `Deferred`:

1. `Deferred` не входил в `permits` — ошибка компиляции в самом интерфейсе.
2. После добавления в `permits` switch без ветки `case Deferred` перестал быть
   исчерпывающим — ошибка компиляции в `Task12Demo.describe`.

Оба места компилятор заставил изменить: границу иерархии и точку обработки.
`default` не понадобился. Никто со стороны не может добавить новый подтип вне
модуля — `permits` закрывает иерархию, поэтому обработка не «протечёт» при
расширении.

---

# Уровень V. Проектирование

## Задание 13. Удаление type code

### Формулировка

Дан класс с `String notificationType` и большим `switch`. Перевести различия
поведения в реализации интерфейса. Не создавать класс на каждую строку
конфигурации: объяснить границу оправданного полиморфизма.

### Алгоритм решения

1. Показать исходный код с type code и `switch`.
2. Выделить интерфейс `NotificationChannel` и реализации каналов.
3. Перенести различия поведения в реализации.
4. Заменить `switch` на вызов через интерфейс.
5. Сформулировать границу: полиморфизм оправдан при смене логики, а не значения.

### Разбор концепций

**Type code** — строковый (или числовой) признак, по которому `switch` выбирает
поведение. При добавлении значения приходится править `switch`, и легко забыть
ветку. **Полиморфизм** заменяет `switch`: каждое поведение живёт в своём классе,
клиент зависит от интерфейса.

**Граница оправданного полиморфизма**: отдельный класс нужен, когда меняется
**логика**. Если различаются только **данные** (например, валюта `"rub"/"usd"`),
достаточно конфигурации или параметра — размножать классы не нужно.

### Ключевые листинги

Было — type code со `switch`:

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
}
```

Стало — интерфейс и клиент без type code:

```java
package task13;

import src.Course;

// различия поведения уехали из switch в реализации интерфейса
public interface NotificationChannel {
  void announce(Course course);
}
```

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

---

## Задание 14. Наследование взрывает комбинации

### Формулировка

Есть форматы курса `Online`, `Classroom`, `Hybrid` и политики оплаты `Free`,
`Fixed`, `Subscription`. Отказаться от девяти подклассов, разделив независимые оси
на компоненты. Продемонстрировать добавление нового формата и новой оплаты с
минимальным изменением существующего кода.

### Алгоритм решения

1. Посчитать число комбинаций: 3 формата × 3 оплаты = 9 классов.
2. Выделить две независимые оси: `CourseFormat` и `PaymentPolicy`.
3. Реализовать варианты по каждой оси отдельно.
4. Собрать предложение `CourseOffer` из двух компонентов.
5. Добавить новый формат и новую оплату без правки `CourseOffer`.

### Разбор концепций

Произведение осей в наследовании даёт **комбинаторный взрыв**. Композиция
независимых осей заменяет произведение **суммой** классов: 3 + 3 + 1 вместо 9.
`CourseOffer` агрегирует два компонента и комбинирует их во время выполнения, а не
на этапе объявления классов. Новая ось добавляется новым классом без изменения
существующего кода — система остаётся открытой для расширения.

### Ключевые листинги

```java
package task14;

// ось формата курса
public interface CourseFormat {
  String describe();
}
```

```java
package task14;

// ось оплаты: независима от формата
public interface PaymentPolicy {
  int price(int basePrice);
}
```

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

Девять комбинаций и новые оси (`SelfPaced`, `Installment`):

```java
package task14;

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

### Формулировка

Спроектировать CourseHub enrollment engine. Правила возвращают типизированный
результат, могут быть открытыми стратегиями или закрытой sealed-иерархией;
композиция правил поддерживает `allOf` и `anyOf`; сервис не проверяет конкретные
классы. Требуется диаграмма зависимостей, минимум пять реализаций, демонстрация
dynamic dispatch и записка: где наследование уместно, где выбрана композиция и
какие гарантии даёт sealed-граница.

### Алгоритм решения

1. Объявить интерфейс `Rule`, возвращающий типизированный результат
   `EnrollmentResult` из задания 12.
2. Реализовать не менее пяти правил.
3. Реализовать композиции `AllOf` и `AnyOf`, сами являющиеся `Rule`.
4. Объявить `EnrollmentEngine`, зависящий только от `Rule`.
5. Собрать правила в цепочку и показать dynamic dispatch и расширение новой
   стратегией.
6. Построить диаграмму зависимостей и записку о выборе наследования и композиции.

### Разбор концепций

Правило — **открытая стратегия**: новый класс не меняет движок. Типизированный
результат — **закрытая sealed-иерархия** из задания 12. Композиции `AllOf` и
`AnyOf` собираются из любых стратегий и сами реализуют `Rule`, поэтому
поддерживают вложенность. Сервис `EnrollmentEngine` не содержит `instanceof` и не
знает конкретных классов правил — весь выбор делает dynamic dispatch.

**Наследование** уместно для закрытой иерархии результата; **композиция** выбрана
для правил, чтобы избежать взрыва комбинаций. **Sealed-граница** гарантирует, что
exhaustive switch по результату всегда полон.

### Диаграмма зависимостей

```text
Task15Demo
   |
   v
EnrollmentEngine ----------> Rule <--------- AllOf / AnyOf
   |                          ^  ^               |
   |                          |  |               | состоит из
   |              +-----------+  +-------+       |
   |              |                       |       v
   |        (открытые стратегии)     (sealed-результат)   [Rule x N]
   |              |                       |
   v              v                       v
src.Course   CourseOpenRule          task12.EnrollmentResult
src.Student  CapacityRule            (sealed: Accepted, Rejected,
             NotEnrolledRule                  WaitListed, Deferred)
             PrerequisiteRule
             AgeRule
```

### Ключевые листинги

Контракт правила — открытая стратегия с типизированным результатом:

```java
package task15;

import src.Course;
import src.Student;
import task12.EnrollmentResult;

// открытая стратегия правила: типизированный результат — sealed EnrollmentResult
public interface Rule {
  EnrollmentResult check(Student student, Course course);
}
```

Композиция `allOf` — возвращает первое отклонение:

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

Композиция `anyOf` — достаточно пройти любому правилу:

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

Сервис без проверки конкретных классов:

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

Пять реализаций правил (`CourseOpenRule`, `CapacityRule`, `NotEnrolledRule`,
`PrerequisiteRule`, `AgeRule`):

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

  public AgeRule(int minAge) { this.minAge = minAge; }

  @Override
  public EnrollmentResult check(Student student, Course course) {
    if (student.age() < minAge) {
      return new Rejected(student.id(), course.id(), "too young, min age " + minAge);
    }
    return new Accepted(student.id(), course.id());
  }
}
```

Демонстрация dynamic dispatch и расширения новой стратегией:

```java
package task15;

import src.Course;
import src.Student;
import task12.Accepted;
import task12.Deferred;
import task12.EnrollmentResult;
import task12.Rejected;
import task12.WaitListed;

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

    print(engine.evaluate(anya, java));  // accepted
    java.addEnrolled(anya.id());
    print(engine.evaluate(anya, java));  // rejected: already enrolled
    print(engine.evaluate(borya, java)); // rejected: too young
    print(engine.evaluate(vera, java));  // rejected: prerequisite missing

    java.setStatus(Course.Status.CLOSED);
    print(engine.evaluate(borya, java)); // rejected: course closed

    // новая политика — новая стратегия, engine не меняется
    java.setStatus(Course.Status.OPEN);
    Rule anyOfAgeOrOpen = new AnyOf(new AgeRule(16), new CourseOpenRule());
    System.out.println("anyOf:");
    print(new EnrollmentEngine(anyOfAgeOrOpen).evaluate(borya, java));
  }
}
```

Вывод — один и тот же `engine` получает отказы от разных правил, что и
демонстрирует dynamic dispatch:

```text
accepted: student 1
rejected: already enrolled
rejected: too young, min age 16
rejected: prerequisite missing
rejected: course closed
anyOf:
accepted: student 2
```

### Записка о выборе механизмов

- **Наследование** уместно в sealed-иерархии результата: варианты — действительно
  один тип с разными данными, и это закрытый набор.
- **Композиция** выбрана для правил: `AllOf`/`AnyOf` собираются из любых стратегий
  без подклассов-комбинаций (в отличие от задания 14, где наследование дало бы
  взрыв количества классов).
- **Гарантия sealed-границы**: внешний код не может добавить вариант, поэтому
  exhaustive switch по результату исчерпывающий по построению: новый вариант
  потребует новую ветку, а не уйдёт молча в `default`. Сами правила остаются
  открытыми стратегиями.

---

## Заключение

В ходе работы последовательно раскрыты ключевые механизмы ООП в Java и границы их
применимости.

**Динамическая диспетчеризация** отделена от **разрешения перегрузок**:
переопределение выбирается JVM по классу объекта, а перегрузка — компилятором по
статическому типу аргумента (задания 1–3). Интерфейс как контракт позволяет
клиенту зависеть только от абстракции и добавлять реализации без правки клиента.

**Абстрактный класс и шаблонный метод** применяются там, где есть общее состояние
или неизменяемый алгоритм; чистое поведение выражается интерфейсом, а различие
только в данных не требует нового типа (задания 4–6).

**Композиция** побеждает наследование в задачах расширения: декораторы заменяют
комбинации подклассов и явно задают порядок поведения (задание 7); независимые оси
заменяют произведение классов суммой (задание 14).

**Контракты подтипов** требуют, чтобы реализация не обещала невозможного: read-only
репозиторий не наследует `save`, а политики допуска подставляются стратегией
(задания 8–10). Нарушение `equals` в иерархии показывает, почему для
value-типов предпочтителен финальный класс/record (задание 11).

**Закрытые иерархии** (`sealed`) дают компилятору полную картину и делают
исчерпывающий `switch` безопасным, тогда как открытые стратегии оставляют систему
расширяемой (задания 12, 15). Итоговый движок правил объединяет оба подхода:
sealed-результат и открытые правила, скреплённые общим контрактом `Rule`.

Сводная таблица «ситуация → механизм»:

| Ситуация | Механизм | Задание |
|---|---|---|
| Нужен общий контракт, много реализаций | `interface` + dynamic dispatch | 1, 3 |
| Нужно разделить выбор по типу | переопределение vs перегрузка | 2, 6 |
| Есть общий алгоритм и точка расширения | `abstract` + template method | 4 |
| Нужно выбрать форму типа | interface / abstract / record / lambda | 5 |
| Нужно добавить поведение обёрткой | декоратор (композиция) | 7 |
| Подтип не может выполнить операцию | разделение интерфейсов (ISP) | 8 |
| Алгоритм подставляется извне | стратегия + внедрение зависимости | 9 |
| Конфликт поведения интерфейсов | явный override | 10 |
| Наследование ломает `equals` | strict equality / композиция / record | 11 |
| Нужна закрытая иерархия вариантов | `sealed` + exhaustive switch | 12 |
| Поведение выбирается строковым кодом | замена `switch` полиморфизмом | 13 |
| Независимые оси образуют комбинации | композиция компонентов | 14 |
| Расширяемый движок с типизированным результатом | стратегии + sealed + композиция | 15 |
