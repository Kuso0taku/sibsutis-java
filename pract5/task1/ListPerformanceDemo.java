import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

// Простой замер, А НЕ микробенчмарк: нет прогрева, JIT и GC не контролируются.
// Цель - показать порядок величин, а не точные числа.
// Чтение по индексу: ArrayList O(1), LinkedList O(n) - разница огромна.
// Обход: обе O(n), но ArrayList быстрее за счет непрерывной памяти.
public class ListPerformanceDemo {
  private static final int N = 100_000;
  private static final int READS = 200_000;

  public static void main(String[] args) {
    List<Integer> arrayList = fill(new ArrayList<>());
    List<Integer> linkedList = fill(new LinkedList<>());

    System.out.println("random access: arrayList=" + randomAccess(arrayList)
        + " ms, linkedList=" + randomAccess(linkedList) + " ms");
    System.out.println("traversal:     arrayList=" + traversal(arrayList)
        + " ms, linkedList=" + traversal(linkedList) + " ms");
  }

  static List<Integer> fill(List<Integer> list) {
    for (int i = 0; i < N; i++) list.add(i);
    return list;
  }

  static long randomAccess(List<Integer> list) {
    Random random = new Random(1);
    long sum = 0;
    long start = System.nanoTime();
    for (int i = 0; i < READS; i++) {
      sum += list.get(random.nextInt(N));
    }
    long elapsed = System.nanoTime() - start;
    if (sum == Long.MIN_VALUE) System.out.print(""); // не дать выкинуть цикл
    return elapsed / 1_000_000;
  }

  static long traversal(List<Integer> list) {
    long sum = 0;
    long start = System.nanoTime();
    for (int value : list) {
      sum += value;
    }
    long elapsed = System.nanoTime() - start;
    if (sum == Long.MIN_VALUE) System.out.print("");
    return elapsed / 1_000_000;
  }
}
