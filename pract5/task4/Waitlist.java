import java.util.ArrayDeque;
import java.util.Queue;

// Очередь ожидания - строгий FIFO. ArrayDeque выбран вместо LinkedList:
// та же сложность O(1) на концах, но без накладных расходов на узлы-объекты.
public class Waitlist {
  private final Queue<Long> queue = new ArrayDeque<>();

  // offer: вернет false, если вставить нельзя. ArrayDeque не ограничен,
  // но код не должен зависеть от реализации очереди.
  public boolean enqueue(long studentId) {
    return queue.offer(studentId);
  }

  // peek: посмотреть следующего, не извлекая; null при пустой очереди
  public Long nextOrNull() {
    return queue.peek();
  }

  // poll: извлечь следующего; null при пустой очереди
  public Long takeOrNull() {
    return queue.poll();
  }

  public int size() {
    return queue.size();
  }
}
