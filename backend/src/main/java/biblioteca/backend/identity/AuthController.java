package biblioteca.backend.identity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Login próprio em JSON: sem o formLogin, o SecurityContext precisa ser salvo na sessão
 * explicitamente, senão a autenticação se perde na requisição seguinte.
 */
@RestController
@RequestMapping("/api")
class AuthController {
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final CsrfTokenRepository csrfTokenRepository;
    private final UserRepository users;
    private final LoginAttemptLimiter loginAttemptLimiter;
    private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

    AuthController(
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            CsrfTokenRepository csrfTokenRepository,
            UserRepository users,
            LoginAttemptLimiter loginAttemptLimiter) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.csrfTokenRepository = csrfTokenRepository;
        this.users = users;
        this.loginAttemptLimiter = loginAttemptLimiter;
    }

    @PostMapping("/auth/login")
    CurrentUserResponse login(
            @Valid @RequestBody LoginRequest body, HttpServletRequest request, HttpServletResponse response) {
        String ip = request.getRemoteAddr();
        if (loginAttemptLimiter.isBlocked(body.username(), ip)) {
            throw new TooManyLoginAttemptsException();
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(body.username(), body.password()));
        } catch (AuthenticationException ex) {
            loginAttemptLimiter.recordFailure(body.username(), ip);
            throw ex;
        }
        loginAttemptLimiter.recordSuccess(body.username(), ip);

        // Evita session fixation.
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        rotateCsrfToken(request, response);

        return CurrentUserResponse.from(users.findByUsername(authentication.getName()).orElseThrow());
    }

    @PostMapping("/auth/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void logout(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        logoutHandler.logout(request, response, authentication);
        rotateCsrfToken(request, response);
    }

    // Lê do banco para que um usuário desativado perca a sessão.
    @GetMapping("/me")
    ResponseEntity<CurrentUserResponse> me(
            Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        return users.findByUsername(authentication.getName())
                .filter(User::isEnabled)
                .map(user -> ResponseEntity.ok(CurrentUserResponse.from(user)))
                .orElseGet(() -> {
                    logout(authentication, request, response);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                });
    }

    // Grava um token novo em vez de só apagar o cookie, para a SPA já ter um na próxima escrita.
    private void rotateCsrfToken(HttpServletRequest request, HttpServletResponse response) {
        csrfTokenRepository.saveToken(csrfTokenRepository.generateToken(request), request, response);
    }

    @ExceptionHandler(TooManyLoginAttemptsException.class)
    ProblemDetail handleTooManyAttempts() {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.TOO_MANY_REQUESTS, "Too many failed sign-in attempts. Try again in 15 minutes.");
    }

    // Mesma mensagem em todos os casos, para não revelar quais usuários existem.
    @ExceptionHandler(AuthenticationException.class)
    ProblemDetail handleAuthenticationFailure() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    static final class TooManyLoginAttemptsException extends RuntimeException {
    }
}
