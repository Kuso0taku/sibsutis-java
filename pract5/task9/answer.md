# Задание 9. PECS на практике

## Метод копирования

```java
static <T> void copy(List<? extends T> source, List<? super T> target)
```

- `source` — **producer**: только читаем, поэтому `? extends T`.
- `target` — **consumer**: только пишем, поэтому `? super T`.

Так `List<Integer>` можно скопировать в `List<Number>`: источник дает
`Integer` (подтип `Number`), приемник принимает `Number` (надтип `Integer`).

## Почему нельзя добавить `Course` в `List<? extends Course>`

`List<? extends Course>` означает «список какого-то неизвестного подтипа
`Course`» — например, `List<AdvancedCourse>` или `List<FreeCourse>`.
Компилятор не знает, какого именно, поэтому запрещает `add`:

```java
List<? extends Course> list = new ArrayList<AdvancedCourse>();
list.add(new Course(...)); // ошибка: вдруг это список FreeCourse?
```

Читать при этом безопасно: любой элемент точно является `Course`, поэтому
`Course c = list.get(0);` компилируется.

## Что можно читать из `List<? super Course>`

`List<? super Course>` — «список `Course` или любого его предка»
(`List<Course>`, `List<Object>`). Туда **можно** писать `Course` и его подтипы,
потому что любой из этих списков примет `Course`.

А вот читать — только как `Object`: компилятор знает лишь, что там лежит
что-то, что является `Course` или предком, но точный тип неизвестен:

```java
List<? super Course> list = new ArrayList<Object>();
list.add(new Course(...)); // можно
Object o = list.get(0);    // можно
Course c = list.get(0);    // ошибка: элемент может не быть Course
```

Итог: producer — `extends`, consumer — `super`.
