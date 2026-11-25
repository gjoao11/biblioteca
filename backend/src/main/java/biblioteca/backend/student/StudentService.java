package biblioteca.backend.student;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
class StudentService {
    static final int PAGE_SIZE = 20;

    private final StudentRepository students;
    private final BranchRepository branches;
    private final StudentCategoryRepository categories;

    StudentService(StudentRepository students, BranchRepository branches, StudentCategoryRepository categories) {
        this.students = students;
        this.branches = branches;
        this.categories = categories;
    }

    @Transactional(readOnly = true)
    List<BranchResponse> listBranches() {
        return branches.findAllByOrderByNameAsc().stream().map(BranchResponse::from).toList();
    }

    @Transactional(readOnly = true)
    List<StudentCategoryResponse> listCategories() {
        return categories.findAllByOrderByNameAsc().stream().map(StudentCategoryResponse::from).toList();
    }

    StudentResponse register(StudentRegistrationRequest request) {
        Branch branch = branches.findById(request.branchId()).orElse(null);
        StudentCategory category = categories.findById(request.categoryId()).orElse(null);
        List<Map<String, String>> missing = new ArrayList<>();
        if (branch == null) {
            missing.add(fieldError("branchId", "does not exist"));
        }
        if (category == null) {
            missing.add(fieldError("categoryId", "does not exist"));
        }
        if (!missing.isEmpty()) {
            throw problem(HttpStatus.BAD_REQUEST, "One or more fields are invalid", missing);
        }

        String rollNumber = request.rollNumber().strip();
        String email = request.email().strip();
        List<Map<String, String>> taken = new ArrayList<>();
        if (students.existsByRollNumber(rollNumber)) {
            taken.add(fieldError("rollNumber", "is already registered"));
        }
        if (students.existsByEmailIgnoreCase(email)) {
            taken.add(fieldError("email", "is already registered"));
        }
        if (!taken.isEmpty()) {
            throw problem(HttpStatus.CONFLICT, "Student is already registered", taken);
        }

        Student student = students.save(new Student(
                request.firstName().strip(), request.lastName().strip(), rollNumber, email,
                branch, category, request.year()));
        return StudentResponse.from(student);
    }

    @Transactional(readOnly = true)
    Page<StudentResponse> list(StudentStatus status, Long branchId, Integer year, Long categoryId, int page) {
        return students.findFiltered(status, branchId, year, categoryId, PageRequest.of(page, PAGE_SIZE))
                .map(StudentResponse::from);
    }

    @Transactional(readOnly = true)
    List<Integer> listYears() {
        return students.findYears();
    }

    @Transactional(readOnly = true)
    StudentResponse get(long id) {
        return students.findWithReferencesById(id).map(StudentResponse::from).orElseThrow(StudentService::notFound);
    }

    StudentResponse decide(long id, StudentStatus decision) {
        Student student = students.findByIdForUpdate(id).orElseThrow(StudentService::notFound);
        if (student.getStatus() != StudentStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Student is not pending approval");
        }
        student.decide(decision);
        return StudentResponse.from(student);
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
    }

    private static Map<String, String> fieldError(String field, String message) {
        return Map.of("field", field, "message", message);
    }

    private static ResponseStatusException problem(HttpStatus status, String detail, List<Map<String, String>> errors) {
        ResponseStatusException ex = new ResponseStatusException(status, detail);
        ex.getBody().setProperty("errors", errors);
        return ex;
    }
}
