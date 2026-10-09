import src.Course;
import src.CourseId;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Контракт ключа: если поле не участвует в equals/hashCode, его изменение
// не ломает поиск в HashSet/HashMap.
public class KeyContractDemo {
  public static void main(String[] args) {
    Course course = new Course(new CourseId(1), "Java", 16);
    course.setPopularity(10);

    Set<Course> set = new HashSet<>();
    set.add(course);
    Map<Course, String> map = new HashMap<>();
    map.put(course, "spring-2026");

    // меняем popularity - поле вне equals/hashCode
    course.setPopularity(999);

    System.out.println("set.contains = " + set.contains(course));
    System.out.println("map.get       = " + map.get(course));
  }
}
