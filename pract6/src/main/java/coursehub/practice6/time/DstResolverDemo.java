package coursehub.practice6.time;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

// пропущенное и повторённое местное время; добавление часа и дня
public class DstResolverDemo {
  public static void main(String[] args) {
    ZoneId zone = ZoneId.of("America/New_York");

    // 10 марта 2024, 02:30 — этого времени в зоне не было
    LocalDateTime missing = LocalDateTime.of(2024, 3, 10, 2, 30);
    System.out.println("gap forward: " + LocalDateTimeResolver.resolveGapForward(missing, zone));
    try {
      LocalDateTimeResolver.resolveStrict(missing, zone);
    } catch (InvalidLocalTimeException e) {
      System.out.println("strict: " + e.getMessage());
    }

    // 3 ноября 2024, 01:30 — это время в зоне встретилось дважды
    LocalDateTime repeated = LocalDateTime.of(2024, 11, 3, 1, 30);
    ZonedDateTime earlier = repeated.atZone(zone);
    ZonedDateTime later = LocalDateTimeResolver.laterOffsetAtOverlap(repeated, zone);
    System.out.println("overlap earlier: " + earlier);
    System.out.println("overlap later:   " + later);

    // добавление часа и дня по-разному проходит через переход
    ZonedDateTime start = ZonedDateTime.of(2024, 3, 10, 1, 30, 0, 0, zone);
    System.out.println("start:        " + start);
    System.out.println("plus 1 hour:  " + start.plusHours(1));
    System.out.println("plus 1 day:   " + start.plusDays(1));
  }
}
