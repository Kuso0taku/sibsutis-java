package task12;

// в лист ожидания
public record WaitListed(long studentId, long courseId, int position) implements EnrollmentResult {
}
