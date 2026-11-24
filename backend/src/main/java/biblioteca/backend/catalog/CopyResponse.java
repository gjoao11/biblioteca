package biblioteca.backend.catalog;

import jakarta.validation.constraints.NotNull;

record CopyResponse(
        @NotNull Long id,
        @NotNull Long bookId,
        @NotNull String title,
        @NotNull String author,
        @NotNull String categoryName,
        @NotNull CopyStatus status) {
}
