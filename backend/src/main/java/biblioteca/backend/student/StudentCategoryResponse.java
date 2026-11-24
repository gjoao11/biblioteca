package biblioteca.backend.student;

import jakarta.validation.constraints.NotNull;

record StudentCategoryResponse(@NotNull Long id, @NotNull String name, @NotNull Integer maxLoans) {
}
