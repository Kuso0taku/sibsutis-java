import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Воспроизведение проблемы: изменяемое поле попало в hashCode.
// После setPopularity тот же объект "теряется" в set и map.
public class BrokenKeyDemo {
  public static void main(String[] args) {
    BadCourse course = new BadCourse(1, 10);

    Set<BadCourse> set = new HashSet<>();
    set.add(course);
    Map<BadCourse, String> map = new HashMap<>();
    map.put(course, "spring-2026");

    System.out.println("before: set.contains = " + set.contains(course)); // true
    course.setPopularity(999); // hash изменился, объект остался в старой корзине
    System.out.println("after:  set.contains = " + set.contains(course)); // false
    System.out.println("after:  map.get       = " + map.get(course));      // null
    System.out.println("after:  set.size      = " + set.size());           // 1 (не пропал!)
  }
}
