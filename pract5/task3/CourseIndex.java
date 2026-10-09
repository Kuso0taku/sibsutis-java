import src.Course;
import src.CourseId;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

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

  // computeIfAbsent: найти или создать одним действием.
  // Фабрика вызывается только при промахе, поэтому дорогое создание
  // объекта не выполняется зря.
  public Course findOrAdd(CourseId id, Supplier<Course> factory) {
    if (id == null) throw new NullPointerException("id must not be null");
    return byId.computeIfAbsent(id, key -> factory.get());
  }

  public int size() {
    return byId.size();
  }
}
