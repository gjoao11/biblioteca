package biblioteca.backend.catalog;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;

import java.util.List;

record PublicBookPageResponse(
        @NotNull List<PublicBookResponse> content,
        @NotNull Integer page,
        @NotNull Integer size,
        @NotNull Long totalElements,
        @NotNull Integer totalPages) {
    static PublicBookPageResponse from(Page<BookSummary> page) {
        return new PublicBookPageResponse(
                page.getContent().stream().map(PublicBookResponse::from).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
