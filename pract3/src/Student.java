package src;

// общая модель студента: используется в task9 и task15
public class Student {
  private final long id;
  private final String name;
  private final int age;
  private final boolean hasPrerequisite;

  public Student(long id, String name, int age, boolean hasPrerequisite) {
    if (id <= 0) throw new IllegalArgumentException("id must be > 0");
    if (name == null || name.isBlank()) throw new IllegalArgumentException("name must not be blank");
    if (age <= 0) throw new IllegalArgumentException("age must be > 0");
    this.id = id;
    this.name = name.trim();
    this.age = age;
    this.hasPrerequisite = hasPrerequisite;
  }

  public long id() { return id; }
  public String name() { return name; }
  public int age() { return age; }
  public boolean hasPrerequisite() { return hasPrerequisite; }
}
