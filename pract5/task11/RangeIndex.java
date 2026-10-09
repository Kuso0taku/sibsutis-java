import src.CourseId;

import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

// Сценарий "диапазонный поиск": нужен поиск по интервалу ключей.
// NavigableMap (TreeMap) дает subMap/headMap/tailMap за O(log n + k),
// чего не умеют HashMap (нет порядка) и ArrayList (только линейный поиск).
public class RangeIndex {
  private final NavigableMap<CourseId, String> byId = new TreeMap<>();

  public void put(CourseId id, String title) {
    byId.put(id, title);
  }

  // все курсы с id в [from, to] включительно
  public SortedMap<CourseId, String> range(long from, long to) {
    if (from > to) throw new IllegalArgumentException("from must be <= to");
    return byId.subMap(new CourseId(from), true, new CourseId(to), true);
  }

  public int size() {
    return byId.size();
  }
}
