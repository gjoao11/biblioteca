package biblioteca.backend.student;

import jakarta.validation.constraints.NotNull;

record BranchResponse(@NotNull Long id, @NotNull String name) {
}
