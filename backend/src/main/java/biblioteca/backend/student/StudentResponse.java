package biblioteca.backend.student;

import jakarta.validation.constraints.NotNull;

record StudentResponse(
        @NotNull Long id,
        @NotNull String firstName,
        @NotNull String lastName,
        @NotNull String rollNumber,
        @NotNull String email,
        @NotNull Long branchId,
        @NotNull String branchName,
        @NotNull Long categoryId,
        @NotNull String categoryName,
        @NotNull Integer year,
        @NotNull StudentStatus status) {
}
