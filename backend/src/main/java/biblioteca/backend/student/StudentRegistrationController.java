package biblioteca.backend.student;

import jakarta.validation.Valid;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/student-registrations")
class StudentRegistrationController {
    private static final Map<String, String> UNIQUE_FIELDS = Map.of(
            "students_roll_number_key", "rollNumber",
            "students_email_lower_idx", "email");

    private final StudentService service;

    StudentRegistrationController(StudentService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    StudentResponse registerStudent(@Valid @RequestBody StudentRegistrationRequest body) {
        return service.register(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleConcurrentDuplicate(DataIntegrityViolationException ex) {
        String field = ex.getCause() instanceof ConstraintViolationException violation
                ? UNIQUE_FIELDS.get(violation.getConstraintName())
                : null;
        if (field == null) {
            throw ex;
        }
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Student is already registered");
        problem.setProperty("errors", List.of(Map.of("field", field, "message", "is already registered")));
        return problem;
    }
}
