package biblioteca.backend.loan;

import jakarta.validation.constraints.NotNull;

record IssueLoanRequest(@NotNull Long studentId, @NotNull Long copyId) {
}
