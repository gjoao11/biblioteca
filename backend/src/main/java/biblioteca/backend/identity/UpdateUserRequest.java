package biblioteca.backend.identity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

record UpdateUserRequest(@NotBlank @Size(max = 100) String name, @NotNull Boolean enabled) {
}
