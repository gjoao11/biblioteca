package biblioteca.backend.loan;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

record LoanResponse(
        @NotNull Long id,
        @NotNull Long copyId,
        @NotNull String bookTitle,
        @NotNull Long studentId,
        @NotNull String studentName,
        @NotNull Instant issuedAt,
        @NotNull Instant dueAt,
        Instant returnedAt) {
}
