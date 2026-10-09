import src.CourseCode;

import java.util.ArrayList;
import java.util.List;

// Список регистраций - это именно List: важен порядок появления.
// ArrayList выбран за O(1) доступ по индексу и хорошую локальность
// при обходе (см. ListPerformanceDemo).
public class CourseRegistry {
  private final List<CourseCode> registrations = new ArrayList<>();

  public void register(CourseCode code) {
    if (code == null) throw new NullPointerException("code must not be null");
    registrations.add(code);
  }

  public void insertAt(int index, CourseCode code) {
    // вставка в середину сохраняет порядок, но в ArrayList это сдвиг хвоста
    registrations.add(index, code);
  }

  public boolean remove(CourseCode code) {
    return registrations.remove(code);
  }

  public CourseCode at(int index) {
    return registrations.get(index);
  }

  public List<CourseCode> sequence() {
    // наружу отдаем копию, а не внутренний изменяемый список
    return List.copyOf(registrations);
  }

  public int size() {
    return registrations.size();
  }
}
