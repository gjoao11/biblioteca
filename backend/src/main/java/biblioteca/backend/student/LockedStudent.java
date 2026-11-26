package biblioteca.backend.student;

public record LockedStudent(Long id, String name, StudentStatus status, int maxLoans) {
}
