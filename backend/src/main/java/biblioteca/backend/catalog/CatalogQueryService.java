package biblioteca.backend.catalog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
class CatalogQueryService {
    static final int PAGE_SIZE = 20;

    private final BookCategoryRepository categories;
    private final BookRepository books;
    private final CopyRepository copies;

    CatalogQueryService(BookCategoryRepository categories, BookRepository books, CopyRepository copies) {
        this.categories = categories;
        this.books = books;
        this.copies = copies;
    }

    List<BookCategory> listCategories() {
        return categories.findAllByOrderByNameAsc();
    }

    Page<BookSummary> listBooks(Long categoryId, String q, int page) {
        return books.findSummaries(categoryId, containsPattern(q), CopyStatus.AVAILABLE, PageRequest.of(page, PAGE_SIZE));
    }

    CopyResponse getCopy(long copyId) {
        return copies.findWithBookById(copyId)
                .map(CopyResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Copy not found"));
    }

    private static String containsPattern(String q) {
        if (q == null || q.isBlank()) {
            return "%";
        }
        String escaped = q.strip().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
