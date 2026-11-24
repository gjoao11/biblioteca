package biblioteca.backend.loan;

import jakarta.validation.constraints.NotNull;

import java.util.List;

record LoanPageResponse(
        @NotNull List<LoanResponse> content,
        @NotNull Integer page,
        @NotNull Integer size,
        @NotNull Long totalElements,
        @NotNull Integer totalPages) {
}
