package biblioteca.backend.student;

import biblioteca.backend.TestFixtures;
import biblioteca.backend.TestcontainersConfiguration;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureMockMvc
class StudentListTests {
    private static final String CSE = "Computer Science and Engineering";
    private static final String CIVIL = "Civil Engineering";

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    EntityManagerFactory entityManagerFactory;

    @Test
    @WithMockUser
    void P08_listsPendingAndApprovedSeparately() throws Exception {
        int year = 1961;
        long pending = student("Pending", year, "PENDING", CSE, "General");
        long approved = student("Approved", year, "APPROVED", CSE, "General");
        student("Rejected", year, "REJECTED", CSE, "General");

        mvc.perform(get("/api/students").param("status", "PENDING").param("year", String.valueOf(year)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(pending))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));

        mvc.perform(get("/api/students").param("status", "APPROVED").param("year", String.valueOf(year)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(approved));
    }

    @Test
    @WithMockUser
    void P08_D9_13_filtersAreOptionalAndCombine() throws Exception {
        int year = 1962;
        long cseGeneral = student("Alice", year, "PENDING", CSE, "General");
        long cseSc = student("Bruno", year, "PENDING", CSE, "SC");
        long civilGeneral = student("Carla", year, "PENDING", CIVIL, "General");
        long cseId = branchId(CSE);
        long generalId = categoryId("General");

        mvc.perform(get("/api/students").param("year", String.valueOf(year)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id")
                        .value(containsInAnyOrder((int) cseGeneral, (int) cseSc, (int) civilGeneral)))
                .andExpect(jsonPath("$.content[*].firstName").value(contains("Alice", "Bruno", "Carla")));

        mvc.perform(get("/api/students").param("year", String.valueOf(year)).param("branch", String.valueOf(cseId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id").value(containsInAnyOrder((int) cseGeneral, (int) cseSc)))
                .andExpect(jsonPath("$.content[0].branchName").value(CSE));

        mvc.perform(get("/api/students").param("year", String.valueOf(year))
                        .param("branch", String.valueOf(cseId)).param("category", String.valueOf(generalId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(cseGeneral))
                .andExpect(jsonPath("$.content[0].categoryName").value("General"));
    }

    @Test
    @WithMockUser
    void D9_13_yearsComeFromTheDatabase() throws Exception {
        student("Year", 1963, "APPROVED", CSE, "General");
        student("Year", 1964, "PENDING", CIVIL, "OBC");

        mvc.perform(get("/api/students/years"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasItems(1963, 1964)))
                .andExpect(jsonPath("$").value(not(hasItems(1951))));

        List<Integer> years = jdbc.queryForList("SELECT DISTINCT year FROM students ORDER BY year", Integer.class);
        mvc.perform(get("/api/students/years"))
                .andExpect(jsonPath("$", hasSize(years.size())));
    }

    @Test
    @WithMockUser
    void D9_20_listIsPaginatedWithTwoQueries() throws Exception {
        int year = 1965;
        for (int i = 0; i < 25; i++) {
            student("Paged%02d".formatted(i), year, "APPROVED", i % 2 == 0 ? CSE : CIVIL, i % 2 == 0 ? "SC" : "ST");
        }
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        mvc.perform(get("/api/students").param("year", String.valueOf(year)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(StudentService.PAGE_SIZE)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(StudentService.PAGE_SIZE))
                .andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content[0].firstName").value("Paged00"))
                .andExpect(jsonPath("$.content[1].branchName").value(CIVIL));

        assertThat(statistics.getPrepareStatementCount()).isEqualTo(2);

        mvc.perform(get("/api/students").param("year", String.valueOf(year)).param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.content[0].firstName").value("Paged20"));
    }

    @Test
    @WithMockUser
    void invalidStatusOrPageIsBadRequest() throws Exception {
        mvc.perform(get("/api/students").param("status", "UNKNOWN")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/students").param("page", "100001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("page"));
    }

    @Test
    @WithMockUser
    void studentDetailHasReferenceNames() throws Exception {
        long id = student("Detail", 1966, "APPROVED", CIVIL, "ST");

        mvc.perform(get("/api/students/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.firstName").value("Detail"))
                .andExpect(jsonPath("$.branchName").value(CIVIL))
                .andExpect(jsonPath("$.categoryName").value("ST"))
                .andExpect(jsonPath("$.year").value(1966))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @WithMockUser
    void unknownStudentIsNotFound() throws Exception {
        mvc.perform(get("/api/students/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Student not found"));
    }

    @Test
    void listsRequireLogin() throws Exception {
        mvc.perform(get("/api/students")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/students/years")).andExpect(status().isUnauthorized());
    }

    private long student(String firstName, int year, String status, String branch, String category) {
        String roll = TestFixtures.unique("R");
        return jdbc.queryForObject("""
                INSERT INTO students (first_name, last_name, roll_number, email, branch_id, category_id, year, status)
                VALUES (?, 'Student', ?, ?, (SELECT id FROM branches WHERE name = ?),
                        (SELECT id FROM student_categories WHERE name = ?), ?, ?)
                RETURNING id
                """, Long.class, firstName, roll, roll + "@example.com", branch, category, year, status);
    }

    private long branchId(String name) {
        return jdbc.queryForObject("SELECT id FROM branches WHERE name = ?", Long.class, name);
    }

    private long categoryId(String name) {
        return jdbc.queryForObject("SELECT id FROM student_categories WHERE name = ?", Long.class, name);
    }
}
