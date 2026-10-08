package src;

public class Course {
    public static final int MIN_DURATION_HOURS = 1;
    public static final int MAX_DURATION_HOURS = 100;

    private long id;
    private String title;
    private int durationHours;
    private int completedHours;

    public Course(long id, String title, int durationHours, int completedHours) {
        if (id <= 0) {
          throw new IllegalArgumentException("id must be > 0");
        }
        this.id = id;
        
        if (!title.isBlank() && !title.isEmpty()) this.title = title.trim();
        else throw new IllegalArgumentException("title must not be blank");

        // границы проверяются тут, а не "молча" игнорируются:
        // иначе курс на 0 часов или на 500 часов выглядел бы как обычный
        checkDuration(durationHours);
        this.durationHours = durationHours;

        if (completedHours >= 0 && completedHours <= durationHours) {
            this.completedHours = completedHours;
        } else {
            throw new IllegalArgumentException(
                "completedHours must be in [0, " + durationHours + "]");
        }
    }

    public Course(long id, String title, int durationHours) {
        this(id, title, durationHours, 0);
    }

    public long id() {
        return this.id;
    }
    public String title() {
        return this.title;
    }
    public int durationHours() {
        return this.durationHours;
    }
    public int completedHours() {
        return this.completedHours;
    }
    public int remainingHours() {
        return durationHours - completedHours;
    }
    public boolean isCompleted() {
        return remainingHours() == 0;
    }

    private static void checkDuration(int durationHours) {
        if (durationHours < MIN_DURATION_HOURS || durationHours > MAX_DURATION_HOURS) {
            throw new IllegalArgumentException(
                "durationHours must be in ["
                    + MIN_DURATION_HOURS + ", " + MAX_DURATION_HOURS + "]");
        }
    }

    public void completeHours(int hours) {
        // неверный аргумент - ошибка вызывающего (programming error)
        if (hours <= 0) {
            throw new IllegalArgumentException("hours must be > 0");
        }
        // слишком много часов - неверное состояние объекта.
        // Проверка до изменения: при отказе completedHours остается прежним.
        if (remainingHours() < hours) {
            throw new IllegalStateException(
                "cannot complete " + hours + " h, only "
                    + remainingHours() + " h left");
        }
        this.completedHours += hours;
    }

    public static void main(String[] args) {
        Course course = new Course(10, "Java Fundamentals", 32); // 0
        course.completeHours(6);

        System.out.println(course.completedHours());
        System.out.println(course.remainingHours());
        System.out.println(course.isCompleted());
    }
}
