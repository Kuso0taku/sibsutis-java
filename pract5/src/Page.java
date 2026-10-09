package src;

import java.util.List;
import java.util.Objects;

// Page<T> - вторая конкретизация generic-идеи: неизменяемая страница выдачи.
// Инварианты проверяются в конструкторе, список копируется,
// чтобы наружу не утекала изменяемая внутренняя коллекция.
public final class Page<T> {
  private final List<T> items;
  private final int pageNumber;
  private final int pageSize;
  private final long totalItems;

  public Page(List<T> items, int pageNumber, int pageSize, long totalItems) {
    if (items == null) throw new NullPointerException("items must not be null");
    if (pageNumber < 0) throw new IllegalArgumentException("pageNumber must be >= 0");
    if (pageSize <= 0) throw new IllegalArgumentException("pageSize must be > 0");
    if (totalItems < items.size()) {
      throw new IllegalArgumentException("totalItems cannot be less than items on the page");
    }
    this.items = List.copyOf(items);
    this.pageNumber = pageNumber;
    this.pageSize = pageSize;
    this.totalItems = totalItems;
  }

  public List<T> items() {
    return items;
  }

  public int pageNumber() {
    return pageNumber;
  }

  public int pageSize() {
    return pageSize;
  }

  public long totalItems() {
    return totalItems;
  }

  public int totalPages() {
    return (int) ((totalItems + pageSize - 1) / pageSize);
  }

  public boolean hasNext() {
    return pageNumber + 1 < totalPages();
  }

  public boolean isEmpty() {
    return items.isEmpty();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Page<?> other)) return false;
    return pageNumber == other.pageNumber
        && pageSize == other.pageSize
        && totalItems == other.totalItems
        && items.equals(other.items);
  }

  @Override
  public int hashCode() {
    return Objects.hash(items, pageNumber, pageSize, totalItems);
  }

  @Override
  public String toString() {
    return "Page[" + pageNumber + "/" + totalPages() + ", items=" + items.size() + "]";
  }
}
