package biblioteca.backend;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class StubEndpointsTests {
    private static final String BOOK = """
            {"title":"T","author":"A","categoryId":1,"copies":1}""";
    private static final String REGISTRATION = """
            {"firstName":"Ana","lastName":"Lima","rollNumber":"1","email":"ana@example.com",\
            "branchId":1,"categoryId":1,"year":2024}""";
    private static final String LOAN = """
            {"studentId":1,"copyId":1}""";
    private static final String RETURN = """
            {"copyId":1}""";

    @Autowired
    MockMvc mvc;

    @ParameterizedTest(name = "{0} {1}")
    @CsvSource(delimiter = '|', textBlock = """
            POST | /api/books                         | BOOK
            GET  | /api/copies/1                      |
            GET  | /api/branches                      |
            GET  | /api/student-categories            |
            POST | /api/public/student-registrations  | REGISTRATION
            GET  | /api/students                      |
            GET  | /api/students/years                |
            GET  | /api/students/1                    |
            POST | /api/students/1/approve            |
            POST | /api/students/1/reject             |
            POST | /api/loans                         | LOAN
            POST | /api/loans/return                  | RETURN
            GET  | /api/loans                         |
            """)
    @WithMockUser
    void stubRespondsNotImplementedWithProblemDetails(String method, String uri, String body) throws Exception {
        var builder = request(HttpMethod.valueOf(method), uri).with(TestCsrf.token(mvc));
        if (body != null) {
            builder.contentType(MediaType.APPLICATION_JSON).content(body(body));
        }
        mvc.perform(builder)
                .andExpect(status().isNotImplemented())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(501))
                .andExpect(jsonPath("$.detail").value("Not implemented yet"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/students", "/api/loans"})
    @WithMockUser
    void pageAboveLimitIsBadRequest(String uri) throws Exception {
        mvc.perform(request(HttpMethod.GET, uri).param("page", "100001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("page"));
    }

    private static String body(String name) {
        return switch (name) {
            case "BOOK" -> BOOK;
            case "REGISTRATION" -> REGISTRATION;
            case "LOAN" -> LOAN;
            case "RETURN" -> RETURN;
            default -> throw new IllegalArgumentException(name);
        };
    }
}
