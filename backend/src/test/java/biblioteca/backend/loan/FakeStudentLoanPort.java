package biblioteca.backend.loan;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Dublê em memória de {@link StudentLoanPort} para testes unitários do LoanService.
 */
class FakeStudentLoanPort implements StudentLoanPort {

    private final Map<Long, StudentSnapshot> store = new HashMap<>();

    void put(StudentSnapshot student) {
        store.put(student.id(), student);
    }

    @Override
    public Optional<StudentSnapshot> findByIdForUpdate(Long studentId) {
        return Optional.ofNullable(store.get(studentId));
    }
}
