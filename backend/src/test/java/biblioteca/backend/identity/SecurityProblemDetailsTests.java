package biblioteca.backend.identity;

import biblioteca.backend.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// O front decide o que fazer pelo status, mas lê a mensagem do corpo: 401 e 403 também vêm em Problem Details.
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class SecurityProblemDetailsTests {
    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository users;

    @Test
    void anonymousOnProtectedEndpointGetsProblemDetails() throws Exception {
        mvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").value("Authentication required"))
                .andExpect(jsonPath("$.instance").value("/api/me"));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void librarianOnAdminEndpointGetsProblemDetails() throws Exception {
        mvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.detail").value("Access denied"))
                .andExpect(jsonPath("$.instance").value("/api/admin/users"));
    }

    @Test
    void writeWithoutCsrfTokenGetsProblemDetails() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"someone\",\"password\":\"secret\"}"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.detail").value("Invalid or missing CSRF token"));
    }

    @Test
    void disabledUserWithOpenSessionGetsProblemDetailsOnMe() throws Exception {
        String username = "disabled-" + System.nanoTime();
        User disabled = new User(username, "Disabled Librarian", "{noop}unused", Role.LIBRARIAN);
        disabled.update("Disabled Librarian", false);
        users.save(disabled);

        mvc.perform(get("/api/me").with(user(username).roles("LIBRARIAN")))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").value("Authentication required"));
    }
}
