package biblioteca.backend.student;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
}
