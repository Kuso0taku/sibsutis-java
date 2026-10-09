import org.junit.jupiter.api.Test;
import src.Comparables;
import src.CourseId;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class ComparablesTest {
  @Test
  void max_worksForInteger() {
    // Arrange
    List<Integer> numbers = List.of(3, 7, 7, 2, 5);

    // Act + Assert
    assertEquals(7, Comparables.max(numbers));
    assertEquals(2, Comparables.min(numbers));
  }

  @Test
  void max_worksForString() {
    // Arrange: лексикографический порядок
    List<String> words = List.of("Java", "Kotlin", "SQL");

    // Act + Assert
    assertEquals("SQL", Comparables.max(words));
    assertEquals("Java", Comparables.min(words));
  }

  @Test
  void max_worksForCustomValueObject() {
    // Arrange: CourseId реализует Comparable<CourseId>
    List<CourseId> ids = List.of(new CourseId(10), new CourseId(3), new CourseId(42));

    // Act + Assert
    assertEquals(new CourseId(42), Comparables.max(ids));
    assertEquals(new CourseId(3), Comparables.min(ids));
  }

  @Test
  void max_acceptsSubtypeWhoseCompareToComesFromSupertype() {
    // Arrange: Derived наследует compareTo от Base, то есть Comparable<Base>
    List<Derived> items = List.of(new Derived(1), new Derived(5), new Derived(3));

    // Act: граница ? super T разрешает это, Comparable<T> - нет
    Derived max = Comparables.max(items);

    // Assert
    assertEquals(5, max.rank);
  }

  @Test
  void max_rejectsEmptyList() {
    // Act + Assert
    assertThrows(NoSuchElementException.class, () -> Comparables.max(List.of()));
  }

  static class Base implements Comparable<Base> {
    final int rank;

    Base(int rank) {
      this.rank = rank;
    }

    @Override
    public int compareTo(Base other) {
      return Integer.compare(rank, other.rank);
    }
  }

  static final class Derived extends Base {
    Derived(int rank) {
      super(rank);
    }
  }
}
