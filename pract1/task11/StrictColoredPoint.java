package task11;

// подтип с цветом: getClass() в equals родителя даёт симметрию false/false
public class StrictColoredPoint extends StrictPoint {
  final String color;

  StrictColoredPoint(int x, int y, String color) {
    super(x, y);
    this.color = color;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    StrictColoredPoint other = (StrictColoredPoint) o;
    return x == other.x && y == other.y && color.equals(other.color);
  }

  @Override
  public int hashCode() {
    return 31 * super.hashCode() + color.hashCode();
  }

  @Override
  public String toString() {
    return "(" + x + "," + y + "," + color + ")";
  }
}
