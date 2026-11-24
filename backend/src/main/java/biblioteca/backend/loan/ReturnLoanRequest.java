package biblioteca.backend.loan;

import jakarta.validation.constraints.NotNull;

record ReturnLoanRequest(@NotNull Long copyId) {
}
