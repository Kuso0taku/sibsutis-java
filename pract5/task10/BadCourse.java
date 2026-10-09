import java.util.Objects;

// Антипример: hashCode включает изменяемое поле popularity.
// Пока объект лежит в HashSet/HashMap, его хеш меняется - корзина, куда
// объект положили, перестает совпадать с текущим хешем.
public class BadCourse {
  private final long id;
  private int popularity;

  public BadCourse(long id, int popularity) {
    this.id = id;
    this.popularity = popularity;
  }

  public void setPopularity(int popularity) {
    this.popularity = popularity;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BadCourse other)) return false;
    return id == other.id && popularity == other.popularity;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, popularity);
  }
}
