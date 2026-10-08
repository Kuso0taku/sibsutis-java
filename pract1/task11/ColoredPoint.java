package task11;

// цвет добавлен через наследование: equals стал несимметричным
public class ColoredPoint extends Point {
  final String color;

  ColoredPoint(int x, int y, String color) {
    super(x, y);
    this.color = color;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ColoredPoint)) return false;
    ColoredPoint other = (ColoredPoint) o;
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
