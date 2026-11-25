package biblioteca.backend.student;

import biblioteca.backend.TestFixtures;
import biblioteca.backend.TestcontainersConfiguration;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class StudentLoanApiTests {
    @Autowired
    StudentLoanApi api;

    @Autowired
    TransactionTemplate tx;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    EntityManagerFactory entityManagerFactory;

    long studentId;

    @BeforeEach
    void setUp() {
        studentId = new TestFixtures(jdbc).student();
        jdbc.update("""
                UPDATE students SET first_name = 'Ana', last_name = 'Lima', status = 'APPROVED',
                    category_id = (SELECT id FROM student_categories WHERE name = 'ST')
                WHERE id = ?""", studentId);
    }

    @Test
    void P09_findsStudentWithNameStatusAndCategoryLimit() {
        LockedStudent student = tx.execute(status -> api.findByIdForUpdate(studentId).orElseThrow());

        assertThat(student).isEqualTo(new LockedStudent(studentId, "Ana Lima", StudentStatus.APPROVED, 4));
    }

    @Test
    void D9_20_findsStudentNamesInOneQueryInsideReadOnlyTransaction() {
        long otherId = new TestFixtures(jdbc).student();
        TransactionTemplate readOnly = new TransactionTemplate(tx.getTransactionManager());
        readOnly.setReadOnly(true);
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        Map<Long, String> names = readOnly.execute(status ->
                api.findStudentNames(List.of(studentId, otherId, Long.MAX_VALUE)));

        assertThat(names).containsExactlyInAnyOrderEntriesOf(Map.of(studentId, "Ana Lima", otherId, "Fixture Student"));
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
    }

    @Test
    void findsStudentNamesWithoutTransaction() {
        assertThat(api.findStudentNames(List.of(studentId))).containsExactly(Map.entry(studentId, "Ana Lima"));
        assertThat(api.findStudentNames(List.of())).isEmpty();
    }

    @Test
    void unknownStudentIsEmpty() {
        Optional<LockedStudent> student = tx.execute(status -> api.findByIdForUpdate(Long.MAX_VALUE));

        assertThat(student).isEmpty();
    }

    @Test
    void D9_18_lockedStudentBlocksOtherTransactions() throws Exception {
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CompletableFuture<Void> holder = CompletableFuture.runAsync(() -> tx.executeWithoutResult(status -> {
            api.findByIdForUpdate(studentId);
            locked.countDown();
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }));
        try {
            assertThat(locked.await(10, TimeUnit.SECONDS)).isTrue();

            assertThatThrownBy(() -> jdbc.queryForList(
                    "SELECT id FROM students WHERE id = ? FOR UPDATE NOWAIT", studentId))
                    .hasMessageContaining("55P03");
            assertThat(jdbc.queryForList(
                    "SELECT id FROM student_categories WHERE name = 'ST' FOR UPDATE NOWAIT")).hasSize(1);
        } finally {
            release.countDown();
            holder.get(10, TimeUnit.SECONDS);
        }
    }

    @Test
    void requiresTheCallerTransaction() {
        assertThatThrownBy(() -> api.findByIdForUpdate(studentId))
                .isInstanceOf(IllegalTransactionStateException.class);
    }
}
