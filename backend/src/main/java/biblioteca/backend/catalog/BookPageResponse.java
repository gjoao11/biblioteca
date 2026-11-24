package biblioteca.backend.catalog;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;

import java.util.List;

record BookPageResponse(
        @NotNull List<BookSummaryResponse> content,
        @NotNull Integer page,
        @NotNull Integer size,
        @NotNull Long totalElements,
        @NotNull Integer totalPages) {
    static BookPageResponse from(Page<BookSummary> page) {
        return new BookPageResponse(
                page.getContent().stream().map(BookSummaryResponse::from).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
