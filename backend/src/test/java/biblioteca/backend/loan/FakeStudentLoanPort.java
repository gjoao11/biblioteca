package biblioteca.backend.loan;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Override
    public Map<Long, String> findStudentNames(Collection<Long> studentIds) {
        return studentIds.stream()
                .filter(store::containsKey)
                .collect(Collectors.toMap(id -> id, id -> store.get(id).name()));
    }
}

