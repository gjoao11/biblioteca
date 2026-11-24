package biblioteca.backend.catalog;

import jakarta.validation.constraints.NotNull;

import java.util.List;

record CreatedBookResponse(
        @NotNull Long id,
        @NotNull String title,
        @NotNull String author,
        String description,
        @NotNull Long categoryId,
        @NotNull String categoryName,
        @NotNull List<Long> copyIds) {
}
