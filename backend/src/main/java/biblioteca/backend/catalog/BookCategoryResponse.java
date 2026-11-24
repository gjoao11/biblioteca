package biblioteca.backend.catalog;

import jakarta.validation.constraints.NotNull;

record BookCategoryResponse(@NotNull Long id, @NotNull String name) {
    static BookCategoryResponse from(BookCategory category) {
        return new BookCategoryResponse(category.getId(), category.getName());
    }
}
