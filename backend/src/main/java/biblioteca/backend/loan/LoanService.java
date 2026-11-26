package biblioteca.backend.loan;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
class LoanService {

    private static final int LOAN_DAYS = 14;

    private final LoanRepository loanRepository;
    private final StudentLoanPort studentPort;
    private final CatalogLoanPort catalogPort;
    private final Clock clock;

    LoanService(LoanRepository loanRepository,
                StudentLoanPort studentPort,
                CatalogLoanPort catalogPort,
                Clock clock) {
        this.loanRepository = loanRepository;
        this.studentPort = studentPort;
        this.catalogPort = catalogPort;
        this.clock = clock;
    }

    // -------------------------------------------------------------------------
    // Emissão de empréstimo
    // -------------------------------------------------------------------------

    @Transactional
    LoanResponse issueLoan(IssueLoanRequest request, Long issuedByUserId) {
        // 1. Aluno existe (trava a linha com FOR UPDATE)
        var student = studentPort.findByIdForUpdate(request.studentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

        // 2. Aluno aprovado
        if (student.status() != StudentLoanPort.StudentSnapshot.Status.APPROVED) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "student not approved");
        }

        // 3. Dentro do limite de empréstimos da categoria
        long openLoans = loanRepository.countByStudentIdAndReturnedAtIsNull(student.id());
        if (openLoans >= student.maxLoans()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "limit reached");
        }

        // 4. Exemplar existe (trava a linha com FOR UPDATE)
        var copy = catalogPort.findCopyByIdForUpdate(request.copyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Copy not found"));

        // 5. Exemplar disponível
        if (!copy.available()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "copy unavailable");
        }

        Instant now = clock.instant();
        Instant dueAt = now.plus(LOAN_DAYS, ChronoUnit.DAYS);

        catalogPort.markLoaned(copy.copyId());
        Loan saved = loanRepository.save(
                new Loan(copy.copyId(), student.id(), issuedByUserId, now, dueAt));

        return toResponse(saved, copy.bookTitle(), student.name());
    }

    // -------------------------------------------------------------------------
    // Devolução
    // -------------------------------------------------------------------------

    @Transactional
    LoanResponse returnLoan(ReturnLoanRequest request) {
        // Trava o exemplar com lock pessimista ANTES de buscar o empréstimo.
        // Sem isso, duas devoluções concorrentes do mesmo exemplar podem ambas
        // encontrar o empréstimo aberto e marcar o exemplar como disponível duas
        // vezes. Se entre elas o exemplar for reemprestado, a segunda devolução
        // marcaria um exemplar emprestado como disponível e o próximo empréstimo
        // daria erro 500.
        catalogPort.findCopyByIdForUpdate(request.copyId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No open loan for this copy"));

        Loan loan = loanRepository.findByCopyIdAndReturnedAtIsNull(request.copyId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No open loan for this copy"));

        loan.markReturned(clock.instant());
        catalogPort.markAvailable(loan.getCopyId());

        // Busca nomes em lote (conjunto de 1 elemento) para reutilizar a mesma API de listagem
        String bookTitle = catalogPort.findBookTitles(List.of(loan.getCopyId()))
                .getOrDefault(loan.getCopyId(), "");
        String studentName = studentPort.findStudentNames(List.of(loan.getStudentId()))
                .getOrDefault(loan.getStudentId(), "");

        return toResponse(loan, bookTitle, studentName);
    }

    // -------------------------------------------------------------------------
    // Listagem paginada
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    LoanPageResponse listLoans(String status, Long studentId, Long copyId, int page) {
        int pageSize = 20;
        var pageable = org.springframework.data.domain.PageRequest.of(page, pageSize);
        var pageResult = loanRepository.findFiltered(status, studentId, copyId, pageable);

        var loans = pageResult.getContent();

        // Busca nomes em lote — evita N+1
        var copyIds = loans.stream().map(Loan::getCopyId).toList();
        var studentIds = loans.stream().map(Loan::getStudentId).toList();
        Map<Long, String> bookTitles = copyIds.isEmpty() ? Map.of() : catalogPort.findBookTitles(copyIds);
        Map<Long, String> studentNames = studentIds.isEmpty() ? Map.of() : studentPort.findStudentNames(studentIds);

        var content = loans.stream()
                .map(loan -> toResponse(
                        loan,
                        bookTitles.getOrDefault(loan.getCopyId(), ""),
                        studentNames.getOrDefault(loan.getStudentId(), "")))
                .toList();

        return new LoanPageResponse(
                content,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static LoanResponse toResponse(Loan loan, String bookTitle, String studentName) {
        return new LoanResponse(
                loan.getId(),
                loan.getCopyId(),
                bookTitle,
                loan.getStudentId(),
                studentName,
                loan.getIssuedAt(),
                loan.getDueAt(),
                loan.getReturnedAt());
    }
}
