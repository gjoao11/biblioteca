package biblioteca.backend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

// Cobre sozinho os endpoints novos. Ao criar um endpoint público, inclua-o em PUBLIC_ENDPOINTS.
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class EndpointSecurityTests {
    private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
            "POST /api/auth/login",
            "POST /api/auth/logout",
            "GET /api/book-categories",
            "GET /api/public/books",
            "GET /api/branches",
            "GET /api/student-categories",
            "POST /api/public/student-registrations");

    @Autowired
    MockMvc mvc;

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    RequestMappingHandlerMapping handlerMapping;

    @Test
    void anonymousIsUnauthorizedOnEveryProtectedEndpoint() throws Exception {
        List<Endpoint> protectedEndpoints = apiEndpoints().stream()
                .filter(endpoint -> !PUBLIC_ENDPOINTS.contains(endpoint.toString()))
                .toList();
        assertThat(protectedEndpoints).isNotEmpty();

        for (Endpoint endpoint : protectedEndpoints) {
            int status = perform(endpoint);
            assertThat(status).as(endpoint.toString()).isEqualTo(401);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("publicEndpoints")
    void anonymousIsNotUnauthorizedOnPublicEndpoint(String endpoint) throws Exception {
        String[] parts = endpoint.split(" ");
        int status = perform(new Endpoint(parts[0], parts[1]));
        assertThat(status).as(endpoint).isNotEqualTo(401);
    }

    static Stream<String> publicEndpoints() {
        return PUBLIC_ENDPOINTS.stream().sorted();
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void librarianIsForbiddenOnEveryAdminEndpoint() throws Exception {
        List<Endpoint> adminEndpoints = apiEndpoints().stream()
                .filter(endpoint -> endpoint.path().startsWith("/api/admin/"))
                .toList();
        assertThat(adminEndpoints).isNotEmpty();

        for (Endpoint endpoint : adminEndpoints) {
            int status = perform(endpoint);
            assertThat(status).as(endpoint.toString()).isEqualTo(403);
        }
    }

    // Com CSRF válido, para medir só a autorização.
    private int perform(Endpoint endpoint) throws Exception {
        String uri = endpoint.path().replaceAll("\\{[^}]+}", "1");
        return mvc.perform(request(HttpMethod.valueOf(endpoint.method()), uri)
                        .with(TestCsrf.token(mvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn().getResponse().getStatus();
    }

    private List<Endpoint> apiEndpoints() {
        List<Endpoint> endpoints = new ArrayList<>();
        handlerMapping.getHandlerMethods().keySet().forEach(info -> {
            Set<RequestMethod> methods = info.getMethodsCondition().getMethods();
            Set<RequestMethod> effective = methods.isEmpty() ? Set.of(RequestMethod.values()) : methods;
            for (String path : info.getPatternValues()) {
                if (!path.startsWith("/api/")) {
                    continue;
                }
                effective.stream()
                        .filter(method -> method != RequestMethod.OPTIONS && method != RequestMethod.HEAD
                                && method != RequestMethod.TRACE)
                        .forEach(method -> endpoints.add(new Endpoint(method.name(), path)));
            }
        });
        return endpoints;
    }

    private record Endpoint(String method, String path) {
        @Override
        public String toString() {
            return method + " " + path;
        }
    }
}
