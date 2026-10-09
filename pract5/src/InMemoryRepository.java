package src;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

// In-memory реализация Repository.
// - внутренний map не отдается наружу: findAll возвращает неизменяемую копию;
// - null не принимается ни на вход, ни как id;
// - повторный save c тем же id = замена (upsert), размер не растет.
public class InMemoryRepository<ID, T> implements Repository<ID, T> {
  private final Map<ID, T> store = new LinkedHashMap<>();
  private final Function<T, ID> idFn;

  public InMemoryRepository(Function<T, ID> idFn) {
    if (idFn == null) throw new NullPointerException("idFn must not be null");
    this.idFn = idFn;
  }

  @Override
  public void save(T entity) {
    if (entity == null) throw new NullPointerException("entity must not be null");
    ID id = idFn.apply(entity);
    if (id == null) throw new NullPointerException("id must not be null");
    store.put(id, entity);
  }

  @Override
  public Optional<T> findById(ID id) {
    if (id == null) throw new NullPointerException("id must not be null");
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public List<T> findAll() {
    return List.copyOf(store.values());
  }

  @Override
  public boolean deleteById(ID id) {
    if (id == null) throw new NullPointerException("id must not be null");
    return store.remove(id) != null;
  }

  public int size() {
    return store.size();
  }
}