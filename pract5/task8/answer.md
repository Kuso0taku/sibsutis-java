# Задание 8. Ограничение типа

## Формулировка границы

```java
<T extends Comparable<? super T>> T max(List<? extends T> items)
```

Почему не просто `T extends Comparable<T>`?

- `? super T` читается как «`compareTo` принимает `T` или любой его предок».
- Это шире, чем `Comparable<T>`, и потому принимает больше корректных типов.
- Классический пример: `java.sql.Timestamp extends java.util.Date`, а
  `compareTo` объявлен в `Date`. С `Comparable<Timestamp>` такой тип не подошел бы.
- Наш `Derived extends Base`, где `Base implements Comparable<Base>`, проходит
  только с `? super T` (см. тест).

## Где проверено

| Тип        | Что проверяем                          |
|------------|----------------------------------------|
| `Integer`  | стандартный `Comparable<Integer>`      |
| `String`   | лексикографический порядок             |
| `CourseId` | собственный value object               |
| `Derived`  | граница работает для подтипов          |

Возвращаем сам элемент (`T`), а не индекс, чтобы вызывающий код не зависел
от внутреннего представления коллекции.
