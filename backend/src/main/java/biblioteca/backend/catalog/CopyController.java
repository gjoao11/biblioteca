package biblioteca.backend.catalog;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/copies")
class CopyController {
    private final CatalogQueryService service;

    CopyController(CatalogQueryService service) {
        this.service = service;
    }

    @GetMapping("/{copyId}")
    CopyResponse getCopy(@PathVariable long copyId) {
        return service.getCopy(copyId);
    }
}
