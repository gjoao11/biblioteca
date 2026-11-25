package biblioteca.backend.catalog;

import biblioteca.backend.TestCsrf;
import biblioteca.backend.TestFixtures;
import biblioteca.backend.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class BookRegistrationTests {
    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    TestFixtures fixtures;
    String username;
    long librarianId;
    long categoryId;

    @BeforeEach
    void setUp() {
        fixtures = new TestFixtures(jdbc);
        username = TestFixtures.unique("librarian");
        librarianId = fixtures.user(username);
        categoryId = fixtures.bookCategory(TestFixtures.unique("registration"));
    }

    @Test
    void P01_createsBookWithAllCopiesAvailable() throws Exception {
        String title = TestFixtures.unique("Clean Code");
        String body = """
                {"title":"  %s  ","author":" Robert Martin ","description":"  ","categoryId":%d,"copies":3}"""
                .formatted(title, categoryId);

        String response = createBook(body)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.author").value("Robert Martin"))
                .andExpect(jsonPath("$.description").doesNotExist())
                .andExpect(jsonPath("$.categoryId").value(categoryId))
                .andExpect(jsonPath("$.copyIds", hasSize(3)))
                .andReturn().getResponse().getContentAsString();

        Long bookId = jdbc.queryForObject("SELECT id FROM books WHERE title = ?", Long.class, title);
        assertThat(response).contains("\"id\":" + bookId);
        assertThat(jdbc.queryForObject("SELECT added_by FROM books WHERE id = ?", Long.class, bookId))
                .isEqualTo(librarianId);
        List<String> statuses = jdbc.queryForList("SELECT status FROM copies WHERE book_id = ?", String.class, bookId);
        assertThat(statuses).containsExactly("AVAILABLE", "AVAILABLE", "AVAILABLE");

        mvc.perform(get("/api/books").param("category", String.valueOf(categoryId)).with(user(username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].totalCopies").value(3))
                .andExpect(jsonPath("$.content[0].availableCopies").value(3));
    }

    @Test
    void P01_D9_22_failedCopyRollsBackTheWholeBook() throws Exception {
        String title = TestFixtures.unique("Atomic");
        jdbc.execute("""
                CREATE FUNCTION reject_third_copy() RETURNS trigger LANGUAGE plpgsql AS $$
                BEGIN
                    IF (SELECT title FROM books WHERE id = NEW.book_id) = '%s'
                       AND (SELECT count(*) FROM copies WHERE book_id = NEW.book_id) >= 2 THEN
                        RAISE EXCEPTION 'third copy rejected';
                    END IF;
                    RETURN NEW;
                END $$""".formatted(title));
        jdbc.execute("CREATE TRIGGER reject_third_copy BEFORE INSERT ON copies "
                + "FOR EACH ROW EXECUTE FUNCTION reject_third_copy()");
        try {
            String body = """
                    {"title":"%s","author":"Someone","categoryId":%d,"copies":3}""".formatted(title, categoryId);

            assertThatThrownBy(() -> createBook(body)).hasStackTraceContaining("third copy rejected");
        } finally {
            jdbc.execute("DROP TRIGGER reject_third_copy ON copies");
            jdbc.execute("DROP FUNCTION reject_third_copy()");
        }

        assertThat(jdbc.queryForObject("SELECT count(*) FROM books WHERE title = ?", Long.class, title)).isZero();
    }

    @Test
    void D9_22_serverRejectsInvalidBookWithFieldErrors() throws Exception {
        long booksBefore = countBooks();

        createBook("""
                {"title":" ","author":"","copies":0}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("One or more fields are invalid"))
                .andExpect(jsonPath("$.errors[*].field")
                        .value(containsInAnyOrder("title", "author", "categoryId", "copies")));

        assertThat(countBooks()).isEqualTo(booksBefore);
    }

    @Test
    void D9_22_rejectsMoreThanOneHundredCopies() throws Exception {
        createBook("""
                {"title":"Too Many","author":"Someone","categoryId":%d,"copies":101}""".formatted(categoryId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("copies"))
                .andExpect(jsonPath("$.errors[0].message").value("must be less than or equal to 100"));
    }

    @Test
    void D9_22_unknownCategoryIsAFieldError() throws Exception {
        long booksBefore = countBooks();

        createBook("""
                {"title":"No Category","author":"Someone","categoryId":%d,"copies":1}""".formatted(Long.MAX_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("One or more fields are invalid"))
                .andExpect(jsonPath("$.errors[0].field").value("categoryId"))
                .andExpect(jsonPath("$.errors[0].message").value("does not exist"));

        assertThat(countBooks()).isEqualTo(booksBefore);
    }

    @Test
    void anonymousCannotCreateBooks() throws Exception {
        mvc.perform(post("/api/books")
                        .with(TestCsrf.token(mvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"T","author":"A","categoryId":%d,"copies":1}""".formatted(categoryId)))
                .andExpect(status().isUnauthorized());
    }

    private ResultActions createBook(String body) throws Exception {
        return mvc.perform(post("/api/books")
                .with(TestCsrf.token(mvc))
                .with(user(username))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private long countBooks() {
        return jdbc.queryForObject("SELECT count(*) FROM books", Long.class);
    }
}
