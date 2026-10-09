import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

// Воспроизведение проблемы: enhanced for использует iterator, но список
// меняется напрямую через remove(). Структурная модификация делает
// modCount != expectedModCount, и следующая проверка итератора падает.
public class ConcurrentModificationDemo {
  public static void main(String[] args) {
    List<String> codes = new ArrayList<>(List.of("JAVA101", "SQL301", "KOTLIN201"));

    try {
      for (String code : codes) {
        if (code.equals("SQL301")) {
          codes.remove(code); // ломает итератор
        }
      }
    } catch (ConcurrentModificationException e) {
      System.out.println("caught: " + e.getClass().getSimpleName());
    }
  }
}
