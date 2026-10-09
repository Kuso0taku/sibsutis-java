import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Generic-модель графа зависимостей: курс -> его prerequisites.
// Соседние множества LinkedHashSet, чтобы перебор был детерминированным
// (порядок вставки, а не хеш-порядок).
public class PrerequisiteGraph<V> {
  private final Map<V, Set<V>> prereqOf = new HashMap<>();
  private final Map<V, Set<V>> dependents = new HashMap<>();
  private final List<V> insertionOrder = new java.util.ArrayList<>();

  // регистрирует вершину явно; повторный вызов - no-op
  public void register(V course) {
    if (course == null) throw new NullPointerException("course must not be null");
    prereqOf.computeIfAbsent(course, k -> {
      insertionOrder.add(k);
      return new LinkedHashSet<>();
    });
    dependents.computeIfAbsent(course, k -> new LinkedHashSet<>());
  }

  // course = dependsOn + prereq = ... несуществующие вершины создаются сами
  public void addPrerequisite(V course, V prereq) {
    register(course);
    register(prereq);
    prereqOf.get(course).add(prereq);
    dependents.get(prereq).add(course);
  }

  public boolean contains(V course) {
    return prereqOf.containsKey(course);
  }

  public int vertexCount() {
    return insertionOrder.size();
  }

  // Топологическая сортировка (Кан). Если граф зациклен - бросаем с путем цикла.
  // Детерминизм: и очередь стартов, и перебор соседей - в порядке вставки,
  // поэтому одинаковый ввод дает одинаковый результат.
  public List<V> order() {
    Map<V, Integer> indegree = new HashMap<>();
    for (V v : insertionOrder) indegree.put(v, 0);
    for (Map.Entry<V, Set<V>> entry : prereqOf.entrySet()) {
      indegree.put(entry.getKey(), entry.getValue().size());
    }

    Deque<V> ready = new ArrayDeque<>();
    for (V v : insertionOrder) {
      if (indegree.get(v) == 0) ready.add(v);
    }

    List<V> result = new java.util.ArrayList<>();
    while (!ready.isEmpty()) {
      V v = ready.poll();
      result.add(v);
      for (V dependent : dependents.getOrDefault(v, Set.of())) {
        int left = indegree.get(dependent) - 1;
        indegree.put(dependent, left);
        if (left == 0) ready.add(dependent);
      }
    }

    if (result.size() != insertionOrder.size()) {
      throw new IllegalStateException(
          "cycle detected: " + findCycle().orElse(List.of()));
    }
    return List.copyOf(result);
  }
}