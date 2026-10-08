package task11;

// исправление 1: strict class equality — сравниваем только объекты своего класса
public class StrictPoint {
  final int x;
  final int y;

  StrictPoint(int x, int y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    StrictPoint other = (StrictPoint) o;
    return x == other.x && y == other.y;
  }

  @Override
  public int hashCode() {
    return 31 * x + y;
  }

  @Override
  public String toString() {
    return "(" + x + "," + y + ")";
  }
}
