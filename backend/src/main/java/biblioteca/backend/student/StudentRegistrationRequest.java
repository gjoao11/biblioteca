package biblioteca.backend.student;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

record StudentRegistrationRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Size(max = 20) String rollNumber,
        @NotBlank @Email @Size(max = 255) String email,
        @NotNull Long branchId,
        @NotNull Long categoryId,
        @NotNull @Min(1950) @Max(2100) Integer year) {
}
