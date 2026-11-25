package biblioteca.backend.student;

import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/student-registrations")
class StudentRegistrationController {
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
    ProblemDetail handleConcurrentDuplicate() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Student is already registered");
    }
}
