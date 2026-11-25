package biblioteca.backend.identity;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class IdentityApi {
    private final UserRepository users;

    IdentityApi(UserRepository users) {
        this.users = users;
    }

    public Optional<Long> findIdByUsername(String username) {
        return users.findByUsername(username).map(User::getId);
    }
}
