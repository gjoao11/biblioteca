package biblioteca.backend.loan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Testes unitários do LoanService usando fakes em memória e mocks.
 * Sem banco de dados, sem Spring context — rápidos e isolados.
 */
class LoanServiceTests {

    static final long LIBRARIAN_ID = 99L;
    static final Instant FIXED_NOW = Instant.parse("2026-01-15T10:00:00Z");

    FakeStudentLoanPort studentPort;
    FakeCatalogLoanPort catalogPort;
    LoanRepository loanRepository;
    LoanService service;

    // Aluno aprovado com max_loans = 2
    StudentLoanPort.StudentSnapshot approvedStudent = new StudentLoanPort.StudentSnapshot(
            1L, "Alice Silva", StudentLoanPort.StudentSnapshot.Status.APPROVED, 2);

    // Aluno pendente
    StudentLoanPort.StudentSnapshot pendingStudent = new StudentLoanPort.StudentSnapshot(
            2L, "Bob Lima", StudentLoanPort.StudentSnapshot.Status.PENDING, 2);

    // Aluno rejeitado
    StudentLoanPort.StudentSnapshot rejectedStudent = new StudentLoanPort.StudentSnapshot(
            3L, "Carlos Melo", StudentLoanPort.StudentSnapshot.Status.REJECTED, 2);

    // Exemplar disponível
    FakeCatalogLoanPort.CopyData availableCopy =
            new FakeCatalogLoanPort.CopyData(10L, "Clean Code", true);

    // Exemplar já emprestado
    FakeCatalogLoanPort.CopyData loanedCopy =
            new FakeCatalogLoanPort.CopyData(11L, "Domain-Driven Design", false);

    @BeforeEach
    void setUp() {
        studentPort = new FakeStudentLoanPort();
        catalogPort = new FakeCatalogLoanPort();
        loanRepository = mock(LoanRepository.class);

        // Defaults
        studentPort.put(approvedStudent);
        studentPort.put(pendingStudent);
        studentPort.put(rejectedStudent);
        catalogPort.put(availableCopy);
        catalogPort.put(loanedCopy);

        Clock fixedClock = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);
        service = new LoanService(loanRepository, studentPort, catalogPort, fixedClock);
    }

    // -------------------------------------------------------------------------
    // issueLoan — caminho feliz
    // -------------------------------------------------------------------------

    @Test
    void P12_issueLoan_setsCorrectDueDate() {
        when(loanRepository.countByStudentIdAndReturnedAtIsNull(1L)).thenReturn(0L);
        when(loanRepository.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(inv -> {
                    Loan l = inv.getArgument(0);
                    // Simula ID gerado pelo banco
                    return new Loan(l.getCopyId(), l.getStudentId(), l.getIssuedBy(),
                            l.getIssuedAt(), l.getDueAt());
                });

        LoanResponse response = service.issueLoan(
                new IssueLoanRequest(approvedStudent.id(), availableCopy.copyId()), LIBRARIAN_ID);

        assertThat(response.issuedAt()).isEqualTo(FIXED_NOW);
        assertThat(response.dueAt()).isEqualTo(FIXED_NOW.plus(14, java.time.temporal.ChronoUnit.DAYS));
        assertThat(response.bookTitle()).isEqualTo("Clean Code");
        assertThat(response.studentName()).isEqualTo("Alice Silva");
        assertThat(catalogPort.isLoaned(availableCopy.copyId())).isTrue();
    }

    // -------------------------------------------------------------------------
    // issueLoan — aluno não encontrado
    // -------------------------------------------------------------------------

    @Test
    void P09_issueLoan_studentNotFound_returns404() {
        assertThatThrownBy(() ->
                service.issueLoan(new IssueLoanRequest(999L, availableCopy.copyId()), LIBRARIAN_ID))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(404));
    }

    // -------------------------------------------------------------------------
    // issueLoan — aluno não aprovado (PENDING / REJECTED)
    // -------------------------------------------------------------------------

    @Test
    void P09_issueLoan_pendingStudent_returns422() {
        assertThatThrownBy(() ->
                service.issueLoan(new IssueLoanRequest(pendingStudent.id(), availableCopy.copyId()), LIBRARIAN_ID))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> {
                    var ex = (ResponseStatusException) e;
                    assertThat(ex.getStatusCode().value()).isEqualTo(422);
                    assertThat(ex.getReason()).isEqualTo("student not approved");
                });
    }

    @Test
    void P09_issueLoan_rejectedStudent_returns422() {
        assertThatThrownBy(() ->
                service.issueLoan(new IssueLoanRequest(rejectedStudent.id(), availableCopy.copyId()), LIBRARIAN_ID))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(422));
    }

    // -------------------------------------------------------------------------
    // issueLoan — limite de empréstimos atingido
    // -------------------------------------------------------------------------

    @Test
    void P11_issueLoan_limitReached_returns422() {
        // max_loans = 2, já tem 2 abertos
        when(loanRepository.countByStudentIdAndReturnedAtIsNull(approvedStudent.id())).thenReturn(2L);

        assertThatThrownBy(() ->
                service.issueLoan(new IssueLoanRequest(approvedStudent.id(), availableCopy.copyId()), LIBRARIAN_ID))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> {
                    var ex = (ResponseStatusException) e;
                    assertThat(ex.getStatusCode().value()).isEqualTo(422);
                    assertThat(ex.getReason()).isEqualTo("limit reached");
                });
    }

    // -------------------------------------------------------------------------
    // issueLoan — exemplar não encontrado
    // -------------------------------------------------------------------------

    @Test
    void issueLoan_copyNotFound_returns404() {
        when(loanRepository.countByStudentIdAndReturnedAtIsNull(approvedStudent.id())).thenReturn(0L);

        assertThatThrownBy(() ->
                service.issueLoan(new IssueLoanRequest(approvedStudent.id(), 999L), LIBRARIAN_ID))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(404));
    }

    // -------------------------------------------------------------------------
    // issueLoan — exemplar indisponível
    // -------------------------------------------------------------------------

    @Test
    void issueLoan_copyUnavailable_returns409() {
        when(loanRepository.countByStudentIdAndReturnedAtIsNull(approvedStudent.id())).thenReturn(0L);

        assertThatThrownBy(() ->
                service.issueLoan(new IssueLoanRequest(approvedStudent.id(), loanedCopy.copyId()), LIBRARIAN_ID))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> {
                    var ex = (ResponseStatusException) e;
                    assertThat(ex.getStatusCode().value()).isEqualTo(409);
                    assertThat(ex.getReason()).isEqualTo("copy unavailable");
                });
    }

    // -------------------------------------------------------------------------
    // returnLoan — exemplar sem empréstimo aberto
    // -------------------------------------------------------------------------

    @Test
    void returnLoan_noOpenLoan_returns404() {
        when(loanRepository.findByCopyIdAndReturnedAtIsNull(availableCopy.copyId()))
                .thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() ->
                service.returnLoan(new ReturnLoanRequest(availableCopy.copyId())))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(404));
    }

    // -------------------------------------------------------------------------
    // issueLoan — verificações executadas na ordem certa
    // -------------------------------------------------------------------------

    @Test
    void issueLoan_checksStudentBeforeCopy() {
        // Aluno pendente + exemplar inexistente → deve falhar no aluno (422), não no exemplar (404)
        assertThatThrownBy(() ->
                service.issueLoan(new IssueLoanRequest(pendingStudent.id(), 999L), LIBRARIAN_ID))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(422));
    }
}
