package task3;

import src.Course;
import task1.CompactCourseFormatter;
import task1.CourseFormatter;
import task1.DetailedCourseFormatter;

// клиент зависит только от интерфейса: ни одного if по типу
public class Task3Demo {
  public static void main(String[] args) {
    Course course = new Course(1, "Java", 10);

    // массив по типу интерфейса
    CourseFormatter[] formatters = {
        new CompactCourseFormatter(),
        new DetailedCourseFormatter(),
        new HtmlCourseFormatter()
    };

    // обход одинаков для любой реализации
    for (CourseFormatter formatter : formatters) {
      System.out.println(formatter.format(course));
    }
  }
}
