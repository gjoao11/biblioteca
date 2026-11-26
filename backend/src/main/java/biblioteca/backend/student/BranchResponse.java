package biblioteca.backend.student;

import jakarta.validation.constraints.NotNull;

record BranchResponse(@NotNull Long id, @NotNull String name) {
    static BranchResponse from(Branch branch) {
        return new BranchResponse(branch.getId(), branch.getName());
    }
}
