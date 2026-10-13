# Dynamic dispatch на бумаге

## Предсказания

| Вызов | Compile-time тип | Runtime класс | Что выполнится |
|---|---|---|---|
| `asInterface.send(...)` | `NotificationSender` | `EmailSender` | `EmailSender.send` — override dispatch по классу объекта |
| `console.send(...)` | `NotificationSender` | `ConsoleSender` | `ConsoleSender.send` |
| `describe(asInterface)` | `NotificationSender` | `EmailSender` | `describe(NotificationSender)` — перегрузка по compile-time типу |
| `describe(asConcrete)` | `EmailSender` | `EmailSender` | `describe(EmailSender)` |

## Overload vs override

- **Override dispatch** разрешается во время выполнения: JVM смотрит на класс объекта.
- **Overload resolution** разрешается при компиляции: компилятор смотрит на статический тип аргумента.

Поэтому `report(asConcrete)` внутри всё равно печатает `by interface`:
перегрузка выбрана по параметру `NotificationSender sender`, хотя объект — `EmailSender`.
