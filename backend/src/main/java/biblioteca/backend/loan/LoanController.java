package biblioteca.backend.loan;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loans")
class LoanController {

    private final LoanService loanService;
    private final LoanUserResolver userResolver;

    LoanController(LoanService loanService, LoanUserResolver userResolver) {
        this.loanService = loanService;
        this.userResolver = userResolver;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    LoanResponse issueLoan(
            @Valid @RequestBody IssueLoanRequest body,
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = userResolver.resolveId(principal);
        return loanService.issueLoan(body, userId);
    }

    @PostMapping("/return")
    LoanResponse returnLoan(@Valid @RequestBody ReturnLoanRequest body) {
        return loanService.returnLoan(body);
    }

    @GetMapping
    LoanPageResponse listLoans(
            @RequestParam(defaultValue = "open") String status,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long copyId,
            @RequestParam(defaultValue = "0") @PositiveOrZero @Max(100_000) int page) {
        return loanService.listLoans(status, studentId, copyId, page);
    }
}
