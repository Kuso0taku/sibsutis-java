package task8;

// полный контракт = чтение + запись
public interface CourseRepository extends CourseReadRepository, CourseWriteRepository {
}
