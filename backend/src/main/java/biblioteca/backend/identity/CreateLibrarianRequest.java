package biblioteca.backend.identity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** O bcrypt só considera os primeiros 72 bytes da senha, daí o limite. */
record CreateLibrarianRequest(
        @NotBlank
        @Size(max = 50)
        @Pattern(regexp = "^[A-Za-z0-9._-]*$", message = "may contain only letters, numbers, dots, hyphens and underscores")
        String username,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(min = 8, max = 72) String password) {
}
