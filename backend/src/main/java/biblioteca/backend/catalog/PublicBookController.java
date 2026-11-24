package biblioteca.backend.catalog;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/books")
class PublicBookController {
    private final CatalogQueryService service;

    PublicBookController(CatalogQueryService service) {
        this.service = service;
    }

    @GetMapping
    PublicBookPageResponse searchPublicBooks(
            @RequestParam(required = false) @Size(max = 255) String q,
            @RequestParam(defaultValue = "0") @PositiveOrZero @Max(100_000) int page) {
        return PublicBookPageResponse.from(service.listBooks(null, q, page));
    }
}
