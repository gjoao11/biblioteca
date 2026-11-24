package biblioteca.backend.catalog;

import jakarta.validation.constraints.NotNull;

record PublicBookResponse(@NotNull String title, @NotNull String author, @NotNull Boolean available) {
    static PublicBookResponse from(BookSummary book) {
        return new PublicBookResponse(book.title(), book.author(), book.availableCopies() > 0);
    }
}
