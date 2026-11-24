package biblioteca.backend.catalog;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/book-categories")
class BookCategoryController {
    private final CatalogQueryService service;

    BookCategoryController(CatalogQueryService service) {
        this.service = service;
    }

    @GetMapping
    List<BookCategoryResponse> listBookCategories() {
        return service.listCategories().stream().map(BookCategoryResponse::from).toList();
    }
}
