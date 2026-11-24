package biblioteca.backend.loan;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/loans")
class LoanController {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    LoanResponse issueLoan(@Valid @RequestBody IssueLoanRequest body) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Not implemented yet");
    }

    @PostMapping("/return")
    LoanResponse returnLoan(@Valid @RequestBody ReturnLoanRequest body) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Not implemented yet");
    }

    @GetMapping
    LoanPageResponse listLoans(
            @RequestParam(defaultValue = "open") String status,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long copyId,
            @RequestParam(defaultValue = "0") @PositiveOrZero @Max(100_000) int page) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Not implemented yet");
    }
}
