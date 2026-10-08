package task3;

import src.Course;
import task1.CourseFormatter;

// третья реализация: добавляется без изменения обхода в Task3Demo
public class HtmlCourseFormatter implements CourseFormatter {
  @Override
  public String format(Course course) {
    return "<b>" + course.title() + "</b>";
  }
}
