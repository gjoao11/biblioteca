package biblioteca.backend.identity;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

record UserResponse(
        @NotNull Long id,
        @NotNull String username,
        @NotNull String name,
        @NotNull Role role,
        @NotNull Boolean enabled,
        @NotNull Instant createdAt) {
    static UserResponse from(User user) {
        return new UserResponse(
                user.getId(), user.getUsername(), user.getName(), user.getRole(), user.isEnabled(), user.getCreatedAt());
    }
}
