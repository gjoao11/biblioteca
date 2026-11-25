package biblioteca.backend.loan;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Resolve o ID numérico do usuário autenticado.
 * Usa JDBC direto para evitar cruzar a fronteira de módulo com identity.
 * Quando identity expuser uma API pública, este componente pode ser adaptado.
 */
@Component
class LoanUserResolver {

    private final JdbcTemplate jdbc;

    LoanUserResolver(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    Long resolveId(UserDetails principal) {
        Long id = jdbc.queryForObject(
                "SELECT id FROM users WHERE username = ?", Long.class, principal.getUsername());
        if (id == null) {
            throw new IllegalStateException("Authenticated user not found: " + principal.getUsername());
        }
        return id;
    }
}
