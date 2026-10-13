package task12;

// отложено: добавлен позже, компилятор потребовал новую ветку в switch (см. answer.md)
public record Deferred(long studentId, long courseId, String semester) implements EnrollmentResult {
}
