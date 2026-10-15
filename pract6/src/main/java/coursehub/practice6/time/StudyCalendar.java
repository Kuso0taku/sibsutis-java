package coursehub.practice6.time;

import java.time.LocalDate;
import java.time.Year;

// календарные расчёты: LocalDate неизменяем — результат операции нужно присвоить
public final class StudyCalendar {
  private final LocalDate start;

  public StudyCalendar(LocalDate start) {
    if (start == null) {
      throw new IllegalArgumentException("start must not be null");
    }
    this.start = start;
  }

  public LocalDate start() {
    return start;
  }

  public boolean isLeapYear() {
    return Year.isLeap(start.getYear());
  }
}
