package src;

import java.util.Objects;

// Идентификатор курса - value object. equals/hashCode по значению,
// поэтому его безопасно использовать как ключ map/set.
public final class CourseId implements Comparable<CourseId> {
  private final long value;

  public CourseId(long value) {
    if (value <= 0) throw new IllegalArgumentException("id must be > 0");
    this.value = value;
  }

  public long value() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CourseId other)) return false;
    return value == other.value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public int compareTo(CourseId other) {
    return Long.compare(value, other.value);
  }

  @Override
  public String toString() {
    return "CourseId(" + value + ")";
  }
}
