import src.Course;
import src.CourseId;
import src.Tag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

// Обратный индекс: по тегу - множество курсов. Поиск по тегу O(1),
// а не полный перебор курсов (как было бы с List или одним Map<Course, Set<Tag>>).
public class InvertedIndex {
  private final Map<Tag, Set<CourseId>> index = new HashMap<>();

  public void add(Course course) {
    if (course == null) throw new NullPointerException("course must not be null");
    for (Tag tag : course.tags()) {
      index.computeIfAbsent(tag, k -> new LinkedHashSet<>()).add(course.id());
    }
  }

  // удаление курса: id уходит из всех множеств; пустой set сразу удаляется
  public void remove(CourseId id, Set<Tag> tags) {
    if (id == null || tags == null) throw new NullPointerException("id and tags must not be null");
    for (Tag tag : tags) {
      Set<CourseId> ids = index.get(tag);
      if (ids == null) continue;
      ids.remove(id);
      if (ids.isEmpty()) {
        index.remove(tag); // инвариант: в индексе не остается пустых set
      }
    }
  }

  // пересечение нескольких тегов: курс подходит, если есть ВСЕ теги
  public Set<CourseId> coursesWithAll(Set<Tag> tags) {
    if (tags.isEmpty()) return Set.of();
    Set<CourseId> result = null;
    for (Tag tag : tags) {
      Set<CourseId> ids = index.get(tag);
      if (ids == null) return Set.of();
      if (result == null) {
        result = new HashSet<>(ids);
      } else {
        result.retainAll(ids);
      }
    }
    return Set.copyOf(result);
  }

  // объединение нескольких тегов: курс подходит, если есть ХОТЯ БЫ один
  public Set<CourseId> coursesWithAny(Set<Tag> tags) {
    Set<CourseId> result = new HashSet<>();
    for (Tag tag : tags) {
      Set<CourseId> ids = index.get(tag);
      if (ids != null) {
        result.addAll(ids);
      }
    }
    return Set.copyOf(result);
  }

  public int tagCount() {
    return index.size();
  }
}