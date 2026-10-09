import src.CourseCode;

import java.util.Iterator;
import java.util.List;

// Два правильных способа удалить во время обхода.
// Оба сообщают итератору об изменении, поэтому modCount согласован.
public final class SafeRemoval {
  private SafeRemoval() {
  }

  // iterator.remove: удаляет текущий элемент, не сдвигая итератор сам по себе
  public static void removeWithIterator(List<CourseCode> codes, CourseCode target) {
    Iterator<CourseCode> iterator = codes.iterator();
    while (iterator.hasNext()) {
      if (iterator.next().equals(target)) {
        iterator.remove();
      }
    }
  }

  // removeIf: та же семантика, но ручной цикл не нужен
  public static void removeWithRemoveIf(List<CourseCode> codes, CourseCode target) {
    codes.removeIf(code -> code.equals(target));
  }
}
