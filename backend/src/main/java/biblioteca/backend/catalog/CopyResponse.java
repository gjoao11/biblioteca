package biblioteca.backend.catalog;

import jakarta.validation.constraints.NotNull;

record CopyResponse(
        @NotNull Long id,
        @NotNull Long bookId,
        @NotNull String title,
        @NotNull String author,
        @NotNull String categoryName,
        @NotNull CopyStatus status) {
    static CopyResponse from(Copy copy) {
        Book book = copy.getBook();
        return new CopyResponse(
                copy.getId(), book.getId(), book.getTitle(), book.getAuthor(), book.getCategory().getName(),
                copy.getStatus());
    }
}
