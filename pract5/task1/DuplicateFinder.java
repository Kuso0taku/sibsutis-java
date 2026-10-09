import src.CourseCode;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Поиск дубликатов одним проходом: seen хранит уже встречавшиеся,
// duplicates - LinkedHashSet, чтобы вернуть их в порядке первого повтора.
public class DuplicateFinder {
  public static List<CourseCode> duplicates(List<CourseCode> codes) {
    Set<CourseCode> seen = new HashSet<>();
    Set<CourseCode> duplicates = new LinkedHashSet<>();
    for (CourseCode code : codes) {
      if (!seen.add(code)) {
        duplicates.add(code);
      }
    }
    return List.copyOf(duplicates);
  }
}
