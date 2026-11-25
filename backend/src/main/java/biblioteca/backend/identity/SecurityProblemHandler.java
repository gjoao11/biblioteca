package biblioteca.backend.identity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.net.URI;

/**
 * Responde 401 e 403 dos filtros do Spring Security em Problem Details. Repassa o erro ao
 * tratamento de exceções do Spring MVC, para o corpo sair igual ao do resto da API.
 */
class SecurityProblemHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    static final String AUTHENTICATION_REQUIRED = "Authentication required";

    private final HandlerExceptionResolver resolver;

    SecurityProblemHandler(HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) {
        resolve(request, response, unauthorized(request, ex));
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) {
        String detail = ex instanceof CsrfException ? "Invalid or missing CSRF token" : "Access denied";
        resolve(request, response, problem(HttpStatus.FORBIDDEN, detail, request, ex));
    }

    static ErrorResponseException unauthorized(HttpServletRequest request, Throwable cause) {
        return problem(HttpStatus.UNAUTHORIZED, AUTHENTICATION_REQUIRED, request, cause);
    }

    private static ErrorResponseException problem(
            HttpStatus status, String detail, HttpServletRequest request, Throwable cause) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setInstance(URI.create(request.getRequestURI()));
        return new ErrorResponseException(status, problem, cause);
    }

    private void resolve(HttpServletRequest request, HttpServletResponse response, ErrorResponseException ex) {
        resolver.resolveException(request, response, null, ex);
    }
}
