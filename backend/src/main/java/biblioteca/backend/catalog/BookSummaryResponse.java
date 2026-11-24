package biblioteca.backend.catalog;

import jakarta.validation.constraints.NotNull;

record BookSummaryResponse(
        @NotNull Long id,
        @NotNull String title,
        @NotNull String author,
        @NotNull Long categoryId,
        @NotNull String categoryName,
        @NotNull Long totalCopies,
        @NotNull Long availableCopies) {
    static BookSummaryResponse from(BookSummary book) {
        return new BookSummaryResponse(
                book.id(), book.title(), book.author(), book.categoryId(), book.categoryName(),
                book.totalCopies(), book.availableCopies());
    }
}
