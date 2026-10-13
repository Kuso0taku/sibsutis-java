package task1;

import src.Course;

// компактный формат: id + название
public class CompactCourseFormatter implements CourseFormatter {
  @Override
  public String format(Course course) {
    return course.id() + " " + course.title();
  }
}
