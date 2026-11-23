package biblioteca.backend.identity;

import biblioteca.backend.TestcontainersConfiguration;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowTests {
    private static final String USERNAME = "auth-flow-librarian";
    private static final String PASSWORD = "correct-password";

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository users;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void createUser() {
        if (users.findByUsername(USERNAME).isEmpty()) {
            users.save(new User(USERNAME, "Auth Flow Librarian", passwordEncoder.encode(PASSWORD), Role.LIBRARIAN));
        }
    }

    @Test
    void fullSessionFlow() throws Exception {
        MvcResult anonymous = mvc.perform(get("/api/me")).andExpect(status().isUnauthorized()).andReturn();
        Cookie csrfCookie = anonymous.getResponse().getCookie("XSRF-TOKEN");
        assertThat(csrfCookie).isNotNull();
        assertThat(csrfCookie.isHttpOnly()).isFalse();
        String csrfToken = csrfCookie.getValue();

        MvcResult login = mvc.perform(post("/api/auth/login")
                        .cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(USERNAME))
                .andExpect(jsonPath("$.name").value("Auth Flow Librarian"))
                .andExpect(jsonPath("$.role").value("LIBRARIAN"))
                .andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        assertThat(session).isNotNull();

        Cookie rotatedCookie = login.getResponse().getCookie("XSRF-TOKEN");
        assertThat(rotatedCookie).isNotNull();
        assertThat(rotatedCookie.getValue()).isNotEqualTo(csrfToken);

        mvc.perform(get("/api/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(USERNAME))
                .andExpect(jsonPath("$.name").value("Auth Flow Librarian"))
                .andExpect(jsonPath("$.role").value("LIBRARIAN"));

        mvc.perform(post("/api/auth/logout")
                        .session(session)
                        .cookie(rotatedCookie)
                        .header("X-XSRF-TOKEN", csrfToken))
                .andExpect(status().isForbidden());

        MvcResult logout = mvc.perform(post("/api/auth/logout")
                        .session(session)
                        .cookie(rotatedCookie)
                        .header("X-XSRF-TOKEN", rotatedCookie.getValue()))
                .andExpect(status().isNoContent())
                .andReturn();

        Cookie afterLogoutCookie = logout.getResponse().getCookie("XSRF-TOKEN");
        assertThat(afterLogoutCookie).isNotNull();
        assertThat(afterLogoutCookie.getValue()).isNotEqualTo(rotatedCookie.getValue());

        assertThat(session.isInvalid()).isTrue();
        mvc.perform(get("/api/me").session(session)).andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithoutCsrfTokenIsForbidden() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(PASSWORD)))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginWithWrongPasswordIsUnauthorized() throws Exception {
        Cookie csrfCookie = fetchCsrfCookie();
        mvc.perform(post("/api/auth/login")
                        .cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfCookie.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("wrong-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid username or password"));
    }

    @Test
    void tooManyFailedLoginsAreRejectedEvenWithTheRightPassword() throws Exception {
        String username = "rate-limited-" + System.nanoTime();
        users.save(new User(username, "Rate Limited", passwordEncoder.encode(PASSWORD), Role.LIBRARIAN));
        Cookie csrfCookie = fetchCsrfCookie();

        for (int i = 0; i < LoginAttemptLimiter.MAX_FAILURES; i++) {
            mvc.perform(post("/api/auth/login")
                            .cookie(csrfCookie)
                            .header("X-XSRF-TOKEN", csrfCookie.getValue())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"%s\",\"password\":\"wrong\"}".formatted(username)))
                    .andExpect(status().isUnauthorized());
        }

        mvc.perform(post("/api/auth/login")
                        .cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfCookie.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, PASSWORD)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.detail").value("Too many failed sign-in attempts. Try again in 15 minutes."));
    }

    @Test
    void loginWithBlankFieldsIsBadRequest() throws Exception {
        Cookie csrfCookie = fetchCsrfCookie();
        mvc.perform(post("/api/auth/login")
                        .cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfCookie.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    private Cookie fetchCsrfCookie() throws Exception {
        return mvc.perform(get("/api/me")).andReturn().getResponse().getCookie("XSRF-TOKEN");
    }

    private static String loginJson(String password) {
        return "{\"username\":\"%s\",\"password\":\"%s\"}".formatted(USERNAME, password);
    }
}
