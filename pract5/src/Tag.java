package src;

import java.util.Locale;

// Тег - тоже value object: "Backend", "backend" и " BACKEND " это один тег.
// Поэтому нормализация и equals/hashCode обязательны (см. task13).
public final class Tag implements Comparable<Tag> {
  private final String name;

  private Tag(String name) {
    this.name = name;
  }

  public static Tag of(String raw) {
    if (raw == null) throw new NullPointerException("tag must not be null");
    String normalized = raw.trim().toLowerCase(Locale.ROOT);
    if (normalized.isEmpty()) {
      throw new IllegalArgumentException("tag must not be blank");
    }
    return new Tag(normalized);
  }

  public String name() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Tag other)) return false;
    return name.equals(other.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public int compareTo(Tag other) {
    return name.compareTo(other.name);
  }

  @Override
  public String toString() {
    return "#" + name;
  }
}
