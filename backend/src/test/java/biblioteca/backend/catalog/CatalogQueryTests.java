package biblioteca.backend.catalog;

import biblioteca.backend.TestFixtures;
import biblioteca.backend.TestcontainersConfiguration;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureMockMvc
class CatalogQueryTests {
    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    EntityManagerFactory entityManagerFactory;

    TestFixtures fixtures;
    long librarianId;

    @BeforeEach
    void setUp() {
        fixtures = new TestFixtures(jdbc);
        librarianId = fixtures.user();
    }

    @Test
    void bookCategoriesArePublicAndComeFromTheSeed() throws Exception {
        mvc.perform(get("/api/book-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name").value(hasItems("Computer Science", "Mathematics", "History")))
                .andExpect(jsonPath("$[0].id").isNumber());
    }

    @Test
    @WithMockUser
    void P03_D9_14_countsTotalAndAvailableCopiesPerBook() throws Exception {
        long category = fixtures.bookCategory(TestFixtures.unique("counts"));
        long mixed = fixtures.book(category, "A Mixed Book", "Author One", librarianId);
        fixtures.copy(mixed, "AVAILABLE");
        fixtures.copy(mixed, "AVAILABLE");
        fixtures.copy(mixed, "LOANED");
        long allLoaned = fixtures.book(category, "B All Loaned", "Author Two", librarianId);
        fixtures.copy(allLoaned, "LOANED");
        fixtures.book(category, "C No Copies", "Author Three", librarianId);

        mvc.perform(get("/api/books").param("category", String.valueOf(category)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].title").value("A Mixed Book"))
                .andExpect(jsonPath("$.content[0].totalCopies").value(3))
                .andExpect(jsonPath("$.content[0].availableCopies").value(2))
                .andExpect(jsonPath("$.content[1].totalCopies").value(1))
                .andExpect(jsonPath("$.content[1].availableCopies").value(0))
                .andExpect(jsonPath("$.content[2].totalCopies").value(0))
                .andExpect(jsonPath("$.content[2].availableCopies").value(0))
                .andExpect(jsonPath("$.content[0].avaliable").doesNotExist());
    }

    @Test
    @WithMockUser
    void P03_filtersByCategory() throws Exception {
        long wanted = fixtures.bookCategory(TestFixtures.unique("wanted"));
        long other = fixtures.bookCategory(TestFixtures.unique("other"));
        fixtures.book(wanted, "Wanted One", "Someone", librarianId);
        fixtures.book(wanted, "Wanted Two", "Someone", librarianId);
        fixtures.book(other, "Other One", "Someone", librarianId);

        mvc.perform(get("/api/books").param("category", String.valueOf(wanted)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[*].title").value(containsInAnyOrder("Wanted One", "Wanted Two")))
                .andExpect(jsonPath("$.content[*].categoryId").value(containsInAnyOrder((int) wanted, (int) wanted)));
    }

    @Test
    @WithMockUser
    void D9_20_listIsPaginated() throws Exception {
        long category = fixtures.bookCategory(TestFixtures.unique("paged"));
        for (int i = 0; i < 25; i++) {
            long book = fixtures.book(category, "Paged Book %02d".formatted(i), "Pager", librarianId);
            fixtures.copy(book, "AVAILABLE");
        }

        mvc.perform(get("/api/books").param("category", String.valueOf(category)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(CatalogQueryService.PAGE_SIZE)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(CatalogQueryService.PAGE_SIZE))
                .andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Paged Book 00"));

        mvc.perform(get("/api/books").param("category", String.valueOf(category)).param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Paged Book 20"))
                .andExpect(jsonPath("$.content[0].totalCopies").value(1));
    }

    @Test
    @WithMockUser
    void D9_20_pageOfBooksUsesTwoQueries() throws Exception {
        long category = fixtures.bookCategory(TestFixtures.unique("queries"));
        for (int i = 0; i < 25; i++) {
            long book = fixtures.book(category, "Counted Book %02d".formatted(i), "Counter", librarianId);
            fixtures.copy(book, "AVAILABLE");
            fixtures.copy(book, "AVAILABLE");
            fixtures.copy(book, "LOANED");
        }
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        mvc.perform(get("/api/books").param("category", String.valueOf(category)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(CatalogQueryService.PAGE_SIZE)))
                .andExpect(jsonPath("$.content[0].availableCopies").value(2));

        assertThat(statistics.getPrepareStatementCount()).isEqualTo(2);
    }

    @Test
    @WithMockUser
    void negativePageIsBadRequest() throws Exception {
        mvc.perform(get("/api/books").param("page", "-1")).andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/books", "/api/public/books"})
    @WithMockUser
    void pageAboveLimitIsBadRequest(String uri) throws Exception {
        mvc.perform(get(uri).param("page", "100001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("page"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/books", "/api/public/books"})
    @WithMockUser
    void queryAbove255CharactersIsBadRequestWithFieldErrors(String uri) throws Exception {
        mvc.perform(get(uri).param("q", "x".repeat(256)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("One or more fields are invalid"))
                .andExpect(jsonPath("$.errors[0].field").value("q"))
                .andExpect(jsonPath("$.errors[0].message").value("size must be between 0 and 255"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @WithMockUser
    void blankQueryListsEveryBookOfTheCategory(String q) throws Exception {
        long category = fixtures.bookCategory(TestFixtures.unique("blank"));
        fixtures.book(category, "Blank One", "Someone", librarianId);
        fixtures.book(category, "Blank Two", "Someone", librarianId);

        mvc.perform(get("/api/books").param("category", String.valueOf(category)).param("q", q))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @WithMockUser
    void unknownCategoryGivesEmptyPage() throws Exception {
        mvc.perform(get("/api/books").param("category", String.valueOf(Long.MAX_VALUE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @WithMockUser
    void P04_searchTreatsUnderscoreAndBackslashAsText() throws Exception {
        String token = TestFixtures.unique("Esc");
        long category = fixtures.bookCategory(TestFixtures.unique("escape"));
        fixtures.book(category, token + " a_b", "Someone", librarianId);
        fixtures.book(category, token + " axb", "Someone", librarianId);
        fixtures.book(category, token + " c\\d", "Someone", librarianId);
        fixtures.book(category, token + " cd", "Someone", librarianId);

        mvc.perform(get("/api/books").param("q", token + " a_b"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value(token + " a_b"));

        mvc.perform(get("/api/books").param("q", token + " c\\d"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value(token + " c\\d"));
    }

    @Test
    @WithMockUser
    void P04_searchesByTitleOrAuthorFragment() throws Exception {
        String token = TestFixtures.unique("Zq");
        long category = fixtures.bookCategory(TestFixtures.unique("search"));
        fixtures.book(category, "Intro to " + token + " Systems", "Plain Author", librarianId);
        fixtures.book(category, "Unrelated Title", "Dr. " + token, librarianId);
        fixtures.book(category, "Not A Match", "Nobody", librarianId);

        mvc.perform(get("/api/books").param("q", token.toLowerCase()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[*].title")
                        .value(containsInAnyOrder("Intro to " + token + " Systems", "Unrelated Title")));
    }

    @Test
    @WithMockUser
    void P04_searchTreatsWildcardsAsText() throws Exception {
        String token = TestFixtures.unique("Wc");
        long category = fixtures.bookCategory(TestFixtures.unique("wildcard"));
        fixtures.book(category, token + " 100% Done", "Someone", librarianId);
        fixtures.book(category, token + " 1000 Done", "Someone", librarianId);

        mvc.perform(get("/api/books").param("q", token + " 100%"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value(token + " 100% Done"));
    }

    @Test
    void anonymousIsUnauthorizedOnBookList() throws Exception {
        mvc.perform(get("/api/books")).andExpect(status().isUnauthorized());
    }

    @Test
    void P05_publicSearchWorksWithoutLogin() throws Exception {
        String token = TestFixtures.unique("Pub");
        long category = fixtures.bookCategory(TestFixtures.unique("public"));
        long available = fixtures.book(category, token + " Available", "Public Author", librarianId);
        fixtures.copy(available, "AVAILABLE");
        fixtures.copy(available, "LOANED");
        long loaned = fixtures.book(category, token + " Loaned", "Public Author", librarianId);
        fixtures.copy(loaned, "LOANED");

        mvc.perform(get("/api/public/books").param("q", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].title").value(token + " Available"))
                .andExpect(jsonPath("$.content[0].available").value(true))
                .andExpect(jsonPath("$.content[1].title").value(token + " Loaned"))
                .andExpect(jsonPath("$.content[1].available").value(false));
    }

    @Test
    void P04_P05_publicSearchHasNoInternalFields() throws Exception {
        String token = TestFixtures.unique("Priv");
        long category = fixtures.bookCategory(TestFixtures.unique("private"));
        long book = fixtures.book(category, token + " Book", "Hidden Author", librarianId);
        fixtures.copy(book, "AVAILABLE");

        mvc.perform(get("/api/public/books").param("q", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value(token + " Book"))
                .andExpect(jsonPath("$.content[0].author").value("Hidden Author"))
                .andExpect(jsonPath("$.content[0].available").value(true))
                .andExpect(jsonPath("$.content[0].id").doesNotExist())
                .andExpect(jsonPath("$.content[0].categoryId").doesNotExist())
                .andExpect(jsonPath("$.content[0].categoryName").doesNotExist())
                .andExpect(jsonPath("$.content[0].totalCopies").doesNotExist())
                .andExpect(jsonPath("$.content[0].availableCopies").doesNotExist())
                .andExpect(jsonPath("$.content[0].description").doesNotExist());
    }
}
