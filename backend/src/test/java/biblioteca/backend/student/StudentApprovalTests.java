package biblioteca.backend.student;

import biblioteca.backend.TestCsrf;
import biblioteca.backend.TestFixtures;
import biblioteca.backend.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class StudentApprovalTests {
    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    @WithMockUser
    void P08_approveMovesStudentFromPendingToApprovedList() throws Exception {
        long id = new TestFixtures(jdbc).student();
        jdbc.update("UPDATE students SET year = 1971 WHERE id = ?", id);

        decide(id, "approve")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.branchName").value("Computer Science and Engineering"));

        assertThat(storedStatus(id)).isEqualTo("APPROVED");
        mvc.perform(get("/api/students").param("status", "PENDING").param("year", "1971"))
                .andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get("/api/students").param("status", "APPROVED").param("year", "1971"))
                .andExpect(jsonPath("$.content[0].id").value(id));
    }

    @Test
    @WithMockUser
    void P08_rejectMarksStudentRejected() throws Exception {
        long id = new TestFixtures(jdbc).student();

        decide(id, "reject")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        assertThat(storedStatus(id)).isEqualTo("REJECTED");
    }

    @ParameterizedTest(name = "{1} from {0}")
    @CsvSource({
            "APPROVED, approve",
            "APPROVED, reject",
            "REJECTED, approve",
            "REJECTED, reject"})
    @WithMockUser
    void decisionOutsidePendingIsConflict(String current, String action) throws Exception {
        long id = new TestFixtures(jdbc).student();
        jdbc.update("UPDATE students SET status = ? WHERE id = ?", current, id);

        decide(id, action)
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Student is not pending approval"));

        assertThat(storedStatus(id)).isEqualTo(current);
    }

    @Test
    @WithMockUser
    void decisionOnUnknownStudentIsNotFound() throws Exception {
        decide(Long.MAX_VALUE, "approve")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Student not found"));
    }

    @Test
    void decisionsRequireLogin() throws Exception {
        long id = new TestFixtures(jdbc).student();

        decide(id, "approve").andExpect(status().isUnauthorized());

        assertThat(storedStatus(id)).isEqualTo("PENDING");
    }

    private ResultActions decide(long id, String action) throws Exception {
        return mvc.perform(post("/api/students/{id}/{action}", id, action).with(TestCsrf.token(mvc)));
    }

    private String storedStatus(long id) {
        return jdbc.queryForObject("SELECT status FROM students WHERE id = ?", String.class, id);
    }
}
