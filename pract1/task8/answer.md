# Нарушение LSP: ReadOnlyCourseRepository

## Проблема (было)

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
`CourseRepository`, получал скрытую бомбу: вызов скомпилируется, но упадёт
в рантайме. Замена `InMemoryCourseRepository` на `ReadOnlyCourseRepository`
ломала поведение — нарушение подстановки Барбары Лисков.

## Исправление

Разделили контракты:

- `CourseReadRepository` — только чтение;
- `CourseWriteRepository` — только запись;
- `CourseRepository extends CourseReadRepository, CourseWriteRepository` — полный набор.

Read-only реализация теперь реализует `CourseReadRepository` и физически
не имеет метода `save`. Невозможная операция не обещается, а вызов
`readOnly.save(...)` не компилируется — ошибка ловится на этапе компиляции,
а не в рантайме.
