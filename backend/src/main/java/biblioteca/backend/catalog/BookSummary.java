package biblioteca.backend.catalog;

record BookSummary(
        Long id,
        String title,
        String author,
        Long categoryId,
        String categoryName,
        Long totalCopies,
        Long availableCopies) {
}
