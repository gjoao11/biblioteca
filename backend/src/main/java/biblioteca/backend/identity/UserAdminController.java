package biblioteca.backend.identity;

import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
class UserAdminController {
    private final UserAdminService service;

    UserAdminController(UserAdminService service) {
        this.service = service;
    }

    @GetMapping
    List<UserResponse> list() {
        return service.list().stream().map(UserResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UserResponse create(@Valid @RequestBody CreateLibrarianRequest body) {
        return UserResponse.from(service.createLibrarian(body));
    }

    @PutMapping("/{id}")
    UserResponse update(@PathVariable long id, @Valid @RequestBody UpdateUserRequest body) {
        return UserResponse.from(service.updateLibrarian(id, body));
    }

    // A checagem no service cobre o caso comum; a constraint UNIQUE cobre duas criações simultâneas.
    @ExceptionHandler({UserAdminService.UsernameTakenException.class, DataIntegrityViolationException.class})
    ProblemDetail handleUsernameTaken() {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Username is already taken");
        problem.setProperty("errors", List.of(Map.of("field", "username", "message", "is already taken")));
        return problem;
    }

    @ExceptionHandler(UserAdminService.UserNotFoundException.class)
    ProblemDetail handleNotFound() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "User not found");
    }

    @ExceptionHandler(UserAdminService.NotALibrarianException.class)
    ProblemDetail handleNotALibrarian() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Only librarian accounts can be changed");
    }
}
