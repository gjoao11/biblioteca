package biblioteca.backend.student;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;

import java.util.List;

record StudentPageResponse(
        @NotNull List<StudentResponse> content,
        @NotNull Integer page,
        @NotNull Integer size,
        @NotNull Long totalElements,
        @NotNull Integer totalPages) {
    static StudentPageResponse from(Page<StudentResponse> page) {
        return new StudentPageResponse(
                page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
