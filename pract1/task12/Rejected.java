package task12;

// отказано с причиной
public record Rejected(long studentId, long courseId, String reason) implements EnrollmentResult {
}
