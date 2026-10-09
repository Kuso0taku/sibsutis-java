package src;

import java.util.List;
import java.util.Optional;

// Обобщенный контракт хранилища. ID и T - параметры типа,
// поэтому один и тот же интерфейс годится и для Course, и для Tag.
public interface Repository<ID, T> {
  // семантика повторного save: upsert (заменить существующее)
  void save(T entity);

  Optional<T> findById(ID id);

  List<T> findAll();

  boolean deleteById(ID id);
}
