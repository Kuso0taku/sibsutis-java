package task11;

// исправление 2, часть 2: цвет — композиция, а не наследование;
// record сам даёт симметричный equals/hashCode
public record Colored<T>(T value, String color) {
}
