package src;

// общая модель курса: не меняется между заданиями, лежит в src/
public class Course {
  public enum Status { OPEN, CLOSED }

  private final long id;
  private final String title;
  private int durationHours;

  private Status status = Status.OPEN;
  private int capacity = 30;
  private boolean hasPrerequisite;
  private final java.util.LinkedHashSet<Long> enrolled = new java.util.LinkedHashSet<>();

  public Course(long id, String title, int durationHours) {
    if (id <= 0) throw new IllegalArgumentException("id must be > 0");
    if (title == null || title.isBlank()) throw new IllegalArgumentException("title must not be blank");
    if (durationHours <= 0) throw new IllegalArgumentException("durationHours must be > 0");
    this.id = id;
    this.title = title.trim();
    this.durationHours = durationHours;
  }

  public long id() { return id; }
  public String title() { return title; }
  public int durationHours() { return durationHours; }

  // поля под задачи демонстрации
  public void setStatus(Status status) { this.status = status; }
  public Status status() { return status; }
  public void setCapacity(int capacity) { this.capacity = capacity; }
  public int capacity() { return capacity; }
  public void setHasPrerequisite(boolean has) { this.hasPrerequisite = has; }
  public boolean hasPrerequisite() { return hasPrerequisite; }

  public java.util.Set<Long> enrolledStudents() {
    return java.util.Collections.unmodifiableSet(enrolled);
  }

  public void addEnrolled(long studentId) { enrolled.add(studentId); }
  public int enrolledCount() { return enrolled.size(); }
  public int seatsLeft() { return capacity - enrolledCount(); }
}
