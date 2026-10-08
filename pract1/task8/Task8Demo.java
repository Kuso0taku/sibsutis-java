package task8;

import src.Course;

// клиент, которому нужно только чтение, зависит от CourseReadRepository
public class Task8Demo {
  public static void main(String[] args) {
    InMemoryCourseRepository full = new InMemoryCourseRepository();
    full.save(new Course(1, "Java", 10));

    CourseReadRepository readOnly = new ReadOnlyCourseRepository(full);
    System.out.println(readOnly.findById(1).map(Course::title).orElse("not found"));

    // не компилируется — и это правильно: read-only не обещает save
    // readOnly.save(new Course(2, "Go", 8));
  }
}
