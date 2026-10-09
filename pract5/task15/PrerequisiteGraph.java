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
}