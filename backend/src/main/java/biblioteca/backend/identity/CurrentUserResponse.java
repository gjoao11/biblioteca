package biblioteca.backend.identity;

import jakarta.validation.constraints.NotNull;

// Sem @NotNull, o springdoc marca os campos como opcionais e os tipos do frontend saem com "?".
record CurrentUserResponse(@NotNull String username, @NotNull String name, @NotNull Role role) {
    static CurrentUserResponse from(User user) {
        return new CurrentUserResponse(user.getUsername(), user.getName(), user.getRole());
    }
}
