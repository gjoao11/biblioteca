package biblioteca.backend.loan;

import biblioteca.backend.student.StudentLoanApi;
import biblioteca.backend.student.StudentStatus;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Adaptador que implementa {@link StudentLoanPort} chamando a API pública do módulo student.
 * Registrado como bean real — substitui o stub {@code @ConditionalOnMissingBean} do {@link LoanConfig}.
 */
@Component
class StudentModuleLoanAdapter implements StudentLoanPort {

    private final StudentLoanApi studentLoanApi;

    StudentModuleLoanAdapter(StudentLoanApi studentLoanApi) {
        this.studentLoanApi = studentLoanApi;
    }

    @Override
    public Optional<StudentSnapshot> findByIdForUpdate(Long studentId) {
        return studentLoanApi.findByIdForUpdate(studentId)
                .map(ls -> new StudentSnapshot(
                        ls.id(),
                        ls.name(),
                        toStatus(ls.status()),
                        ls.maxLoans()));
    }

    @Override
    public Map<Long, String> findStudentNames(Collection<Long> studentIds) {
        return studentLoanApi.findStudentNames(studentIds);
    }

    private static StudentSnapshot.Status toStatus(StudentStatus status) {
        return switch (status) {
            case PENDING -> StudentSnapshot.Status.PENDING;
            case APPROVED -> StudentSnapshot.Status.APPROVED;
            case REJECTED -> StudentSnapshot.Status.REJECTED;
        };
    }
}
