package biblioteca.backend.catalog;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@Transactional
class BookService {
    private final BookCategoryRepository categories;
    private final BookRepository books;
    private final CopyRepository copies;

    BookService(BookCategoryRepository categories, BookRepository books, CopyRepository copies) {
        this.categories = categories;
        this.books = books;
        this.copies = copies;
    }

    CreatedBookResponse createBook(CreateBookRequest request, String username) {
        BookCategory category = categories.findById(request.categoryId())
                .orElseThrow(() -> invalidField("categoryId", "does not exist"));
        Long addedBy = books.findUserIdByUsername(username).orElseThrow();
        Book book = books.save(new Book(
                request.title().strip(), request.author().strip(), description(request.description()),
                category, addedBy));
        List<Long> copyIds = copies.saveAll(Stream.generate(() -> new Copy(book)).limit(request.copies()).toList())
                .stream().map(Copy::getId).toList();
        return new CreatedBookResponse(
                book.getId(), book.getTitle(), book.getAuthor(), book.getDescription(),
                category.getId(), category.getName(), copyIds);
    }

    private static String description(String description) {
        return description == null || description.isBlank() ? null : description.strip();
    }

    private static ResponseStatusException invalidField(String field, String message) {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more fields are invalid");
        ex.getBody().setProperty("errors", List.of(Map.of("field", field, "message", message)));
        return ex;
    }
}
