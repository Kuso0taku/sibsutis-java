package task1;

import src.Course;

// компилятор знает только CourseFormatter.format(Course);
// какая реализация вызовется — решается во время выполнения по классу объекта
public class Task1Demo {
  public static void main(String[] args) {
    Course course = new Course(1, "Java", 10);

    // обе реализации через переменную интерфейсного типа
    CourseFormatter compact = new CompactCourseFormatter();
    CourseFormatter detailed = new DetailedCourseFormatter();

    System.out.println(compact.format(course));
    System.out.println(detailed.format(course));
  }
}
