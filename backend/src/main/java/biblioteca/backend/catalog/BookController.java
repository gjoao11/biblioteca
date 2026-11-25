package biblioteca.backend.catalog;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
class BookController {
    private final CatalogQueryService service;
    private final BookService bookService;

    BookController(CatalogQueryService service, BookService bookService) {
        this.service = service;
        this.bookService = bookService;
    }

    @GetMapping
    BookPageResponse listBooks(
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) @Size(max = 255) String q,
            @RequestParam(defaultValue = "0") @PositiveOrZero @Max(100_000) int page) {
        return BookPageResponse.from(service.listBooks(category, q, page));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CreatedBookResponse createBook(@Valid @RequestBody CreateBookRequest body, Authentication authentication) {
        return bookService.createBook(body, authentication.getName());
    }
}
