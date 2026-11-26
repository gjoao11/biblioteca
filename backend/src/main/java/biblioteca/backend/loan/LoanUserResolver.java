package biblioteca.backend.loan;

import biblioteca.backend.identity.IdentityApi;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Resolve o ID numérico do usuário autenticado chamando a API pública do módulo identity.
 */
@Component
class LoanUserResolver {

    private final IdentityApi identityApi;

    LoanUserResolver(IdentityApi identityApi) {
        this.identityApi = identityApi;
    }

    Long resolveId(UserDetails principal) {
        return identityApi.findIdByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user not found: " + principal.getUsername()));
    }
}
