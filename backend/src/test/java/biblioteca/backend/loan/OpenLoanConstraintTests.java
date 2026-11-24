package biblioteca.backend.loan;

import biblioteca.backend.TestFixtures;
import biblioteca.backend.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class OpenLoanConstraintTests {
    @Autowired
    JdbcTemplate jdbc;

    TestFixtures fixtures;
    long librarianId;
    long copyId;

    @BeforeEach
    void setUp() {
        fixtures = new TestFixtures(jdbc);
        librarianId = fixtures.user();
        long categoryId = fixtures.bookCategory(TestFixtures.unique("loan-category"));
        long bookId = fixtures.book(categoryId, "Loan Constraint Book", "Some Author", librarianId);
        copyId = fixtures.copy(bookId, "AVAILABLE");
    }

    @Test
    void D9_18_partialIndexRejectsSecondOpenLoan() {
        fixtures.openLoan(copyId, fixtures.student(), librarianId);
        long otherStudent = fixtures.student();

        assertThatThrownBy(() -> fixtures.openLoan(copyId, otherStudent, librarianId))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("loans_one_open_per_copy_idx");
        assertThat(jdbc.queryForObject(
                "SELECT count(*) FROM loans WHERE copy_id = ? AND returned_at IS NULL", Long.class, copyId))
                .isEqualTo(1);
    }

    @Test
    void returnedLoanDoesNotBlockANewOne() {
        long studentId = fixtures.student();
        fixtures.returnLoan(fixtures.openLoan(copyId, studentId, librarianId));

        fixtures.openLoan(copyId, studentId, librarianId);

        assertThat(jdbc.queryForObject("SELECT count(*) FROM loans WHERE copy_id = ?", Long.class, copyId))
                .isEqualTo(2);
    }

    @Test
    void loanRequiresExistingCopy() {
        long studentId = fixtures.student();
        assertThatThrownBy(() -> fixtures.openLoan(Long.MAX_VALUE, studentId, librarianId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
