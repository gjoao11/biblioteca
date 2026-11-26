package biblioteca.backend.catalog;

import biblioteca.backend.TestFixtures;
import biblioteca.backend.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class CopyDetailTests {
    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    @WithMockUser
    void copyDetailShowsBookAndStatus() throws Exception {
        TestFixtures fixtures = new TestFixtures(jdbc);
        String categoryName = TestFixtures.unique("detail");
        long category = fixtures.bookCategory(categoryName);
        long book = fixtures.book(category, "Detail Book", "Detail Author", fixtures.user());
        long copy = fixtures.copy(book, "LOANED");

        mvc.perform(get("/api/copies/{copyId}", copy))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(copy))
                .andExpect(jsonPath("$.bookId").value(book))
                .andExpect(jsonPath("$.title").value("Detail Book"))
                .andExpect(jsonPath("$.author").value("Detail Author"))
                .andExpect(jsonPath("$.categoryName").value(categoryName))
                .andExpect(jsonPath("$.status").value("LOANED"));
    }

    @Test
    @WithMockUser
    void unknownCopyIsNotFoundWithProblemDetails() throws Exception {
        mvc.perform(get("/api/copies/{copyId}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Copy not found"));
    }

    @Test
    void D9_02_copyDetailRequiresLogin() throws Exception {
        mvc.perform(get("/api/copies/1")).andExpect(status().isUnauthorized());
    }
}
