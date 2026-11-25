package biblioteca.backend.student;

import jakarta.validation.constraints.NotNull;

record StudentCategoryResponse(@NotNull Long id, @NotNull String name, @NotNull Integer maxLoans) {
    static StudentCategoryResponse from(StudentCategory category) {
        return new StudentCategoryResponse(category.getId(), category.getName(), category.getMaxLoans());
    }
}
