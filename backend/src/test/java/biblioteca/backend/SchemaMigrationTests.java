package biblioteca.backend;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class SchemaMigrationTests {
    @Autowired
    Flyway flyway;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void migrationsApplyFromScratch() {
        List<String> applied = Arrays.stream(flyway.info().applied())
                .filter(info -> info.getState().isApplied() && !info.getState().isFailed())
                .map(MigrationInfo::getVersion)
                .map(Object::toString)
                .toList();
        assertThat(applied).containsExactly("1", "2", "3", "4");
        assertThat(flyway.info().pending()).isEmpty();
    }

    @Test
    void referenceDataIsSeeded() {
        List<Map<String, Object>> categories = jdbc.queryForList(
                "SELECT name, max_loans FROM student_categories ORDER BY id");
        assertThat(categories).extracting(row -> row.get("name")).containsExactly("General", "OBC", "SC", "ST");
        assertThat(categories).allSatisfy(row -> assertThat((Integer) row.get("max_loans")).isPositive());

        assertThat(jdbc.queryForObject("SELECT count(*) FROM book_categories", Long.class)).isGreaterThanOrEqualTo(7);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM branches", Long.class)).isEqualTo(5);
    }

    @Test
    void D9_27_studentsHaveNoLoanCounter() {
        List<String> columns = jdbc.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'students' ORDER BY ordinal_position",
                String.class);
        assertThat(columns).containsExactly(
                "id", "first_name", "last_name", "roll_number", "email",
                "branch_id", "category_id", "year", "status", "created_at");
    }

    @Test
    void D9_16_studentEmailIsUniqueIgnoringCase() {
        TestFixtures fixtures = new TestFixtures(jdbc);
        String local = TestFixtures.unique("ana");
        fixtures.student(local.toUpperCase() + "@Example.com");

        assertThatThrownBy(() -> fixtures.student(local + "@example.com"))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("students_email_lower_idx");
    }
}
