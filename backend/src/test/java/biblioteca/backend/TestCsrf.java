package biblioteca.backend;

import jakarta.servlet.http.Cookie;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Use no lugar do {@code csrf()} do spring-security-test, que troca o repositório de tokens no
 * contexto compartilhado e faz o cookie XSRF-TOKEN sumir para os testes seguintes.
 */
public final class TestCsrf {
    private TestCsrf() {
    }

    public static RequestPostProcessor token(MockMvc mvc) throws Exception {
        Cookie cookie = mvc.perform(get("/actuator/health")).andReturn().getResponse().getCookie("XSRF-TOKEN");
        if (cookie == null) {
            throw new IllegalStateException("XSRF-TOKEN cookie was not issued");
        }
        return request -> {
            request.setCookies(cookie);
            request.addHeader("X-XSRF-TOKEN", cookie.getValue());
            return request;
        };
    }
}
