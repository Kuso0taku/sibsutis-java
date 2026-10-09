import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

// Сценарий "отсортированный leaderboard": нужен порядок по счету.
// Выбор - TreeMap (красно-черное дерево): вставка/поиск O(log n),
// а первый/последний элемент и "топ-N" достаются без полной сортировки.
public class Leaderboard {
  // обратный порядок: больший счет - первым
  private final NavigableMap<Integer, List<String>> byScore =
      new TreeMap<>(Collections.reverseOrder());

  public void score(String student, int points) {
    byScore.computeIfAbsent(points, k -> new ArrayList<>()).add(student);
  }

  public List<String> top(int n) {
    List<String> result = new ArrayList<>();
    for (Map.Entry<Integer, List<String>> entry : byScore.entrySet()) {
      for (String student : entry.getValue()) {
        if (result.size() == n) return List.copyOf(result);
        result.add(student);
      }
    }
    return List.copyOf(result);
  }

  public int bestScore() {
    return byScore.isEmpty() ? -1 : byScore.firstKey();
  }

  // LinkedHashMap лишь для наглядного снимка "как хранится"
  public Map<Integer, List<String>> snapshot() {
    return new LinkedHashMap<>(byScore);
  }
}
