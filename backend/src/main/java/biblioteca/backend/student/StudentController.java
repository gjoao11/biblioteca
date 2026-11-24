package biblioteca.backend.student;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/students")
class StudentController {
    @GetMapping
    StudentPageResponse listStudents(
            @RequestParam(required = false) StudentStatus status,
            @RequestParam(required = false) Long branch,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Long category,
            @RequestParam(defaultValue = "0") @PositiveOrZero @Max(100_000) int page) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Not implemented yet");
    }

    @GetMapping("/years")
    List<Integer> listStudentYears() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Not implemented yet");
    }

    @GetMapping("/{id}")
    StudentResponse getStudent(@PathVariable long id) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Not implemented yet");
    }

    @PostMapping("/{id}/approve")
    StudentResponse approveStudent(@PathVariable long id) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Not implemented yet");
    }

    @PostMapping("/{id}/reject")
    StudentResponse rejectStudent(@PathVariable long id) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Not implemented yet");
    }
}
