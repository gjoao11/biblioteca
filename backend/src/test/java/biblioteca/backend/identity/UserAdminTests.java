package biblioteca.backend.identity;

import biblioteca.backend.TestCsrf;
import biblioteca.backend.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class UserAdminTests {
    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository users;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCreatesLibrarianWhoCanSignIn() throws Exception {
        String username = uniqueUsername();

        mvc.perform(post("/api/admin/users").with(TestCsrf.token(mvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(username, "Maria Silva", "librarian-pass")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.name").value("Maria Silva"))
                .andExpect(jsonPath("$.role").value("LIBRARIAN"))
                .andExpect(jsonPath("$.enabled").value(true));

        mvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].username").value(hasItem(username)));

        mvc.perform(post("/api/auth/login").with(TestCsrf.token(mvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(username, "librarian-pass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("LIBRARIAN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void duplicateUsernameIsConflictOnTheUsernameField() throws Exception {
        String username = uniqueUsername();
        users.save(new User(username, "Existing", passwordEncoder.encode("whatever-pass"), Role.LIBRARIAN));

        mvc.perform(post("/api/admin/users").with(TestCsrf.token(mvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(username, "Other", "librarian-pass")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors[0].field").value("username"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void invalidFieldsAreBadRequest() throws Exception {
        mvc.perform(post("/api/admin/users").with(TestCsrf.token(mvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("has space", "", "short")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(3));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void disabledLibrarianCannotSignIn() throws Exception {
        String username = uniqueUsername();
        User librarian = users.save(
                new User(username, "To Disable", passwordEncoder.encode("librarian-pass"), Role.LIBRARIAN));

        mvc.perform(put("/api/admin/users/{id}", librarian.getId()).with(TestCsrf.token(mvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Renamed\",\"enabled\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renamed"))
                .andExpect(jsonPath("$.enabled").value(false));

        mvc.perform(post("/api/auth/login").with(TestCsrf.token(mvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(username, "librarian-pass")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminAccountsCannotBeChangedHere() throws Exception {
        User admin = users.save(
                new User(uniqueUsername(), "Another Admin", passwordEncoder.encode("admin-pass-123"), Role.ADMIN));

        mvc.perform(put("/api/admin/users/{id}", admin.getId()).with(TestCsrf.token(mvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Hacked\",\"enabled\":false}"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void unknownUserIsNotFound() throws Exception {
        mvc.perform(put("/api/admin/users/{id}", Long.MAX_VALUE).with(TestCsrf.token(mvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nobody\",\"enabled\":true}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void librarianIsForbidden() throws Exception {
        mvc.perform(get("/api/admin/users")).andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/users").with(TestCsrf.token(mvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(uniqueUsername(), "Sneaky", "librarian-pass")))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousIsUnauthorized() throws Exception {
        mvc.perform(get("/api/admin/users")).andExpect(status().isUnauthorized());
    }

    private static String uniqueUsername() {
        return "user-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private static String createJson(String username, String name, String password) {
        return "{\"username\":\"%s\",\"name\":\"%s\",\"password\":\"%s\"}".formatted(username, name, password);
    }

    private static String loginJson(String username, String password) {
        return "{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password);
    }
}
