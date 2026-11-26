package biblioteca.backend.student;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
class StudentReferenceController {
    private final StudentService service;

    StudentReferenceController(StudentService service) {
        this.service = service;
    }

    @GetMapping("/branches")
    List<BranchResponse> listBranches() {
        return service.listBranches();
    }

    @GetMapping("/student-categories")
    List<StudentCategoryResponse> listStudentCategories() {
        return service.listCategories();
    }
}
