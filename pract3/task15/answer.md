# Расширяемый движок правил CourseHub

## Диаграмма зависимостей

```
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

## Пять реализаций правил

`CourseOpenRule`, `CapacityRule`, `NotEnrolledRule`, `PrerequisiteRule`,
`AgeRule` — плюс композиции `AllOf` и `AnyOf` сами являются `Rule`.

## Dynamic dispatch

`engine.evaluate(...)` вызывает `rule.check(...)` — какой код выполнится,
решается во время выполнения по классу объекта. В `Task15Demo` один и тот же
`EnrollmentEngine` прогоняет студентов через `AllOf` из пяти разных классов:
отказ приходит от разных правил, engine об этом не знает. Замена политики
на `AnyOf` — новый объект, engine не изменён.

## Где наследование, где композиция

- **Наследование** уместно в sealed-иерархии результата: варианты — это
  действительно один тип `EnrollmentResult` с разными данными, и это закрытый набор.
- **Композиция** выбрана для правил: `AllOf`/`AnyOf` собираются из любых
  стратегий без подклассов-комбинаций (в отличие от task14, где наследование
  давало бы взрыв количества классов).

## Гарантия sealed-границы

Результат — `sealed interface EnrollmentResult`. Внешний код не может добавить
вариант, поэтому exhaustive switch в `Task15Demo.print` исчерпывающий по
построению: новый вариант потребует new ветку, а не уйдёт молча в `default`.
Сами правила остаются открытыми стратегиями: новые проверки добавляются
новыми классами без изменения sealed-границы и движка.
