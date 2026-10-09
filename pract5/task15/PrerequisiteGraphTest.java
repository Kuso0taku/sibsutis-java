import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrerequisiteGraphTest {
  @Test
  void order_respectsAllDependencies() {
    // Arrange: Java <- (Base, OOP), OOP <- Base
    PrerequisiteGraph<String> graph = new PrerequisiteGraph<>();
    graph.addPrerequisite("Java", "Base");
    graph.addPrerequisite("Java", "OOP");
    graph.addPrerequisite("OOP", "Base");

    // Act
    List<String> order = graph.order();

    // Assert: prerequisite всегда раньше курса
    assertTrue(order.indexOf("Base") < order.indexOf("Java"));
    assertTrue(order.indexOf("Base") < order.indexOf("OOP"));
    assertTrue(order.indexOf("OOP") < order.indexOf("Java"));
    assertEquals(3, order.size());
  }

  @Test
  void order_isDeterministicForTheSameInput() {
    // Arrange: два одинаковых графа, построенных одинаково
    PrerequisiteGraph<String> first = new PrerequisiteGraph<>();
    first.addPrerequisite("C", "A");
    first.addPrerequisite("C", "B");
    first.addPrerequisite("B", "A");

    PrerequisiteGraph<String> second = new PrerequisiteGraph<>();
    second.addPrerequisite("C", "A");
    second.addPrerequisite("C", "B");
    second.addPrerequisite("B", "A");

    // Act + Assert: одинаковый ввод -> одинаковый результат
    assertEquals(first.order(), second.order());
  }

  @Test
  void cycleIsDetected_andDiagnosticPathIsReported() {
    // Arrange: A -> B -> C -> A (цикл)
    PrerequisiteGraph<String> graph = new PrerequisiteGraph<>();
    graph.addPrerequisite("A", "B");
    graph.addPrerequisite("B", "C");
    graph.addPrerequisite("C", "A");

    // Act
    IllegalStateException e = assertThrows(IllegalStateException.class, graph::order);

    // Assert: сообщение показывает путь цикла
    List<String> path = graph.findCycle().orElseThrow();
    assertEquals(path.get(0), path.get(path.size() - 1), "путь замкнут");
    assertTrue(path.size() >= 2);
    assertTrue(path.containsAll(List.of("A", "B", "C")));
    assertTrue(e.getMessage().contains("cycle"));
  }

  @Test
  void selfLoop_isAlsoACycle() {
    // Arrange
    PrerequisiteGraph<String> graph = new PrerequisiteGraph<>();
    graph.addPrerequisite("A", "A");

    // Act + Assert
    assertEquals(List.of("A", "A"), graph.findCycle().orElseThrow());
    assertThrows(IllegalStateException.class, graph::order);
  }

  @Test
  void acyclicGraph_hasNoCyclePath() {
    // Arrange
    PrerequisiteGraph<String> graph = new PrerequisiteGraph<>();
    graph.addPrerequisite("Java", "Base");

    // Act + Assert
    assertTrue(graph.findCycle().isEmpty());
  }

  @Test
  void missingVertex_isNotContained_untilRegistered() {
    // Arrange
    PrerequisiteGraph<String> graph = new PrerequisiteGraph<>();
    graph.addPrerequisite("Java", "Base");

    // Assert: незарегистрированная вершина отсутствует
    assertFalse(graph.contains("Kotlin"));
    assertTrue(graph.contains("Java"));
    assertEquals(2, graph.vertexCount());

    // Act: addPrerequisite сам регистрирует несуществующие вершины
    graph.addPrerequisite("Kotlin", "Java");

    // Assert
    assertTrue(graph.contains("Kotlin"));
    assertEquals(3, graph.vertexCount());
  }

  @Test
  void order_returnsEveryRegisteredVertex() {
    // Arrange
    PrerequisiteGraph<String> graph = new PrerequisiteGraph<>();
    graph.addPrerequisite("Java", "Base");
    graph.register("SQL");

    // Act
    List<String> order = graph.order();

    // Assert: даже независимая вершина не теряется
    assertEquals(3, order.size());
    assertTrue(order.containsAll(List.of("Java", "Base", "SQL")));
  }
}