package biblioteca.backend.identity;

import biblioteca.backend.TestFixtures;
import biblioteca.backend.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class IdentityApiTests {
    @Autowired
    IdentityApi api;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void findsIdOfExistingUser() {
        String username = TestFixtures.unique("identity-api");
        long id = new TestFixtures(jdbc).user(username);

        assertThat(api.findIdByUsername(username)).contains(id);
    }

    @Test
    void unknownUsernameIsEmpty() {
        assertThat(api.findIdByUsername(TestFixtures.unique("missing"))).isEmpty();
    }
}
