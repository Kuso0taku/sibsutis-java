import src.CourseCode;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Удаление дубликатов. Выбрана LinkedHashSet:
// HashSet дал бы уникальность, но потерял порядок,
// TreeSet отсортировал бы, а нам нужен именно порядок первого появления.
public final class Deduplicator {
  private Deduplicator() {
  }

  public static List<CourseCode> unique(List<CourseCode> codes) {
    Set<CourseCode> set = new LinkedHashSet<>();
    for (CourseCode code : codes) {
      if (code == null) throw new NullPointerException("code must not be null");
      set.add(code);
    }
    return List.copyOf(set);
  }

  // Наивный способ "в лоб" - для сравнения сложности: O(n^2) против O(n) у set.
  public static List<CourseCode> uniqueNaive(List<CourseCode> codes) {
    List<CourseCode> result = new ArrayList<>();
    for (CourseCode code : codes) {
      if (!result.contains(code)) {
        result.add(code);
      }
    }
    return result;
  }
}
