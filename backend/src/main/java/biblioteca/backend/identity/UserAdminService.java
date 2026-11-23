package biblioteca.backend.identity;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
class UserAdminService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    UserAdminService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    List<User> list() {
        return users.findAllByOrderByNameAsc();
    }

    User createLibrarian(CreateLibrarianRequest request) {
        if (users.existsByUsername(request.username())) {
            throw new UsernameTakenException();
        }
        return users.save(new User(
                request.username(), request.name().strip(), passwordEncoder.encode(request.password()), Role.LIBRARIAN));
    }

    User updateLibrarian(long id, UpdateUserRequest request) {
        User user = users.findById(id).orElseThrow(UserNotFoundException::new);
        if (user.getRole() != Role.LIBRARIAN) {
            throw new NotALibrarianException();
        }
        user.update(request.name().strip(), request.enabled());
        return user;
    }

    static final class UsernameTakenException extends RuntimeException {
    }

    static final class UserNotFoundException extends RuntimeException {
    }

    static final class NotALibrarianException extends RuntimeException {
    }
}
