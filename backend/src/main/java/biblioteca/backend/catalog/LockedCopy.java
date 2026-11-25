package biblioteca.backend.catalog;

public record LockedCopy(Long copyId, String bookTitle, boolean available) {
}
