package src;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

// Общая модель курса. equals/hashCode намеренно основаны только на id:
// title, duration и popularity могут меняться, а "ключ" курса в коллекциях
// остается стабильным. Именно на этом строится task10.
public class Course {
  private final CourseId id;
  private final String title;
  private final int durationHours;

  // не участвует в равенстве - можно менять, не ломая HashMap/HashSet
  private int popularity;

  private final Set<Tag> tags = new LinkedHashSet<>();

  public Course(CourseId id, String title, int durationHours) {
    if (id == null) throw new NullPointerException("id must not be null");
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("title must not be blank");
    }
    if (durationHours <= 0) {
      throw new IllegalArgumentException("durationHours must be > 0");
    }
    this.id = id;
    this.title = title.trim();
    this.durationHours = durationHours;
  }

  public CourseId id() {
    return id;
  }

  public String title() {
    return title;
  }

  public int durationHours() {
    return durationHours;
  }

  public int popularity() {
    return popularity;
  }

  public void setPopularity(int popularity) {
    this.popularity = popularity;
  }

  public void addTag(Tag tag) {
    if (tag == null) throw new NullPointerException("tag must not be null");
    tags.add(tag);
  }

  public Set<Tag> tags() {
    return Collections.unmodifiableSet(tags);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Course other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "Course[" + id + ", " + title + "]";
  }
}
