package task1;

import src.Course;

// подробный формат: название, часы, статус
public class DetailedCourseFormatter implements CourseFormatter {
  @Override
  public String format(Course course) {
    return course.title() + " (" + course.durationHours() + " h, " + course.status() + ")";
  }
}
