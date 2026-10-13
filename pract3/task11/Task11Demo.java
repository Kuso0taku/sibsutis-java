package task11;

import java.util.HashSet;
import java.util.Set;

// нарушение симметричности + два исправления
public class Task11Demo {
  public static void main(String[] args) {
    // == нарушение симметричности ==
    Point p = new Point(1, 2);
    ColoredPoint cp = new ColoredPoint(1, 2, "red");

    System.out.println("p.equals(cp)   = " + p.equals(cp));   // true: цвет не важен Point'у
    System.out.println("cp.equals(p)   = " + cp.equals(p));   // false: p не ColoredPoint

    // HashSet теряет элементы из-за несимметричного equals/hashCode
    Set<Point> set = new HashSet<>();
    set.add(p);
    set.add(cp);
    System.out.println("set size = " + set.size()); // 2, хотя для Point'а они равны

    // == исправление 1: strict class equality ==
    StrictPoint sp = new StrictPoint(1, 2);
    StrictColoredPoint scp = new StrictColoredPoint(1, 2, "red");
    System.out.println("strict: sp.equals(scp) = " + sp.equals(scp));  // false
    System.out.println("strict: scp.equals(sp) = " + scp.equals(sp));  // false: симметрия

    // == исправление 2: композиция цвета ==
    Colored<PlainPoint> left = new Colored<>(new PlainPoint(1, 2), "red");
    Colored<PlainPoint> same = new Colored<>(new PlainPoint(1, 2), "red");
    Colored<PlainPoint> other = new Colored<>(new PlainPoint(1, 2), "blue");
    System.out.println("compose: left.equals(same) = " + left.equals(same)); // true
    System.out.println("compose: left.equals(other) = " + left.equals(other)); // false
  }
}
