import src.Course;
import src.CourseId;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

// Индекс CourseId -> Course. LinkedHashMap выбран, чтобы findAll()
// отдавал курсы в порядке добавления.
public class CourseIndex {
  private final Map<CourseId, Course> byId = new LinkedHashMap<>();

  public void add(Course course) {
    if (course == null) throw new NullPointerException("course must not be null");
    // putIfAbsent не перезаписывает молча: результат == null значит "вставили",
    // иначе такой id уже есть и это ошибка вызывающего кода
    Course previous = byId.putIfAbsent(course.id(), course);
    if (previous != null) {
      throw new IllegalStateException("duplicate id: " + course.id());
    }
  }

  public void replace(Course course) {
    // явная команда замены - намерение видно в имени метода
    if (course == null) throw new NullPointerException("course must not be null");
    byId.put(course.id(), course);
  }

  public Optional<Course> find(CourseId id) {
    return Optional.ofNullable(byId.get(id));
  }

  public boolean contains(CourseId id) {
    return byId.containsKey(id);
  }

  public int size() {
    return byId.size();
  }
}
