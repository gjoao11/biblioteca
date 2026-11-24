package biblioteca.backend.student;

import jakarta.validation.constraints.NotNull;

import java.util.List;

record StudentPageResponse(
        @NotNull List<StudentResponse> content,
        @NotNull Integer page,
        @NotNull Integer size,
        @NotNull Long totalElements,
        @NotNull Integer totalPages) {
}
