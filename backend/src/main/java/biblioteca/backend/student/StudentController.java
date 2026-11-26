package biblioteca.backend.student;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/students")
class StudentController {
    private final StudentService service;

    StudentController(StudentService service) {
        this.service = service;
    }

    @GetMapping
    StudentPageResponse listStudents(
            @RequestParam(required = false) StudentStatus status,
            @RequestParam(required = false) Long branch,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Long category,
            @RequestParam(defaultValue = "0") @PositiveOrZero @Max(100_000) int page) {
        return StudentPageResponse.from(service.list(status, branch, year, category, page));
    }

    @GetMapping("/years")
    List<Integer> listStudentYears() {
        return service.listYears();
    }

    @GetMapping("/{id}")
    StudentResponse getStudent(@PathVariable long id) {
        return service.get(id);
    }

    @PostMapping("/{id}/approve")
    StudentResponse approveStudent(@PathVariable long id) {
        return service.decide(id, StudentStatus.APPROVED);
    }

    @PostMapping("/{id}/reject")
    StudentResponse rejectStudent(@PathVariable long id) {
        return service.decide(id, StudentStatus.REJECTED);
    }
}
