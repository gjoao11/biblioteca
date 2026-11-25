package biblioteca.backend.student;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(propagation = Propagation.MANDATORY)
public class StudentLoanApi {
    private final StudentRepository students;

    StudentLoanApi(StudentRepository students) {
        this.students = students;
    }

    public Optional<LockedStudent> findByIdForUpdate(Long studentId) {
        return students.findByIdForUpdate(studentId).map(student -> new LockedStudent(
                student.getId(), student.getName(), student.getStatus(), student.getCategory().getMaxLoans()));
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Map<Long, String> findStudentNames(Collection<Long> studentIds) {
        if (studentIds.isEmpty()) {
            return Map.of();
        }
        return students.findNames(studentIds).stream()
                .collect(Collectors.toMap(StudentName::id, StudentName::name));
    }
}
