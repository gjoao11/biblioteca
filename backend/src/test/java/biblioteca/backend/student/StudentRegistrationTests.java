package biblioteca.backend.student;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class StudentRegistrationTests {
    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    long branchId;
    long categoryId;

    @BeforeEach
    void setUp() {
        branchId = jdbc.queryForObject("SELECT id FROM branches WHERE name = 'Civil Engineering'", Long.class);
        categoryId = jdbc.queryForObject("SELECT id FROM student_categories WHERE name = 'SC'", Long.class);
    }

    @Test
    void referenceListsArePublicAndComeFromTheSeed() throws Exception {
        mvc.perform(get("/api/branches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name").value(hasItems("Civil Engineering", "Computer Science and Engineering")))
                .andExpect(jsonPath("$[0].id").isNumber());

        mvc.perform(get("/api/student-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name").value(hasItems("General", "OBC", "SC", "ST")))
                .andExpect(jsonPath("$[?(@.name == 'SC')].maxLoans").value(hasItems(4)));
    }

    @Test
    void P07_studentRegistersWithoutLoginAndStaysPending() throws Exception {
        String roll = TestFixtures.unique("R");

        register(body(" Ana ", " Lima ", " " + roll + " ", roll + "@Example.com", branchId, categoryId, 2024))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.firstName").value("Ana"))
                .andExpect(jsonPath("$.lastName").value("Lima"))
                .andExpect(jsonPath("$.rollNumber").value(roll))
                .andExpect(jsonPath("$.email").value(roll + "@Example.com"))
                .andExpect(jsonPath("$.branchId").value(branchId))
                .andExpect(jsonPath("$.branchName").value("Civil Engineering"))
                .andExpect(jsonPath("$.categoryName").value("SC"))
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.status").value("PENDING"));

        assertThat(jdbc.queryForObject("SELECT status FROM students WHERE roll_number = ?", String.class, roll))
                .isEqualTo("PENDING");
    }

    @Test
    void D9_16_invalidRegistrationListsEachField() throws Exception {
        register("""
                {"firstName":"","lastName":" ","rollNumber":"","email":"not-an-email","year":1800}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("One or more fields are invalid"))
                .andExpect(jsonPath("$.errors[*].field").value(containsInAnyOrder(
                        "firstName", "lastName", "rollNumber", "email", "branchId", "categoryId", "year")));
    }

    @Test
    void D9_16_rollNumberLongerThanTwentyCharactersIsRejected() throws Exception {
        register(body("Ana", "Lima", "R".repeat(21), TestFixtures.unique("long") + "@example.com",
                branchId, categoryId, 2024))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("rollNumber"))
                .andExpect(jsonPath("$.errors[0].message").value("size must be between 0 and 20"));
    }

    @Test
    void D9_16_unknownBranchAndCategoryAreFieldErrors() throws Exception {
        String roll = TestFixtures.unique("R");

        register(body("Ana", "Lima", roll, roll + "@example.com", Long.MAX_VALUE, Long.MAX_VALUE, 2024))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("One or more fields are invalid"))
                .andExpect(jsonPath("$.errors[*].field").value(containsInAnyOrder("branchId", "categoryId")))
                .andExpect(jsonPath("$.errors[0].message").value("does not exist"));

        assertThat(jdbc.queryForObject("SELECT count(*) FROM students WHERE roll_number = ?", Long.class, roll))
                .isZero();
    }

    @Test
    void D9_16_duplicateRollNumberIsConflict() throws Exception {
        String roll = TestFixtures.unique("R");
        register(body("Ana", "Lima", roll, roll + "@example.com", branchId, categoryId, 2024))
                .andExpect(status().isCreated());

        register(body("Bia", "Souza", roll, roll + "-other@example.com", branchId, categoryId, 2024))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Student is already registered"))
                .andExpect(jsonPath("$.errors[*].field").value(containsInAnyOrder("rollNumber")))
                .andExpect(jsonPath("$.errors[0].message").value("is already registered"));
    }

    @Test
    void D9_16_duplicateEmailIgnoringCaseIsConflict() throws Exception {
        String local = TestFixtures.unique("mail");
        register(body("Ana", "Lima", TestFixtures.unique("R"), local + "@example.com", branchId, categoryId, 2024))
                .andExpect(status().isCreated());

        register(body("Bia", "Souza", TestFixtures.unique("R"), local.toUpperCase() + "@EXAMPLE.COM",
                branchId, categoryId, 2024))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors[*].field").value(containsInAnyOrder("email")));
    }

    @Test
    void D9_16_bothDuplicatesAreReported() throws Exception {
        String roll = TestFixtures.unique("R");
        register(body("Ana", "Lima", roll, roll + "@example.com", branchId, categoryId, 2024))
                .andExpect(status().isCreated());

        register(body("Ana", "Lima", roll, roll + "@example.com", branchId, categoryId, 2024))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors[*].field").value(containsInAnyOrder("rollNumber", "email")));
    }

    private ResultActions register(String body) throws Exception {
        return mvc.perform(post("/api/public/student-registrations")
                .with(TestCsrf.token(mvc))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private static String body(String firstName, String lastName, String roll, String email,
                               long branchId, long categoryId, int year) {
        return """
                {"firstName":"%s","lastName":"%s","rollNumber":"%s","email":"%s",\
                "branchId":%d,"categoryId":%d,"year":%d}"""
                .formatted(firstName, lastName, roll, email, branchId, categoryId, year);
    }
}
