package biblioteca.backend.catalog;

import biblioteca.backend.TestFixtures;
import biblioteca.backend.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class CatalogLoanApiTests {
    @Autowired
    CatalogLoanApi api;

    @Autowired
    TransactionTemplate tx;

    @Autowired
    JdbcTemplate jdbc;

    long copyId;

    @BeforeEach
    void setUp() {
        TestFixtures fixtures = new TestFixtures(jdbc);
        long category = fixtures.bookCategory(TestFixtures.unique("loan-api"));
        long book = fixtures.book(category, "Lockable Book", "Someone", fixtures.user());
        copyId = fixtures.copy(book, "AVAILABLE");
    }

    @Test
    void findsCopyWithBookTitleAndAvailability() {
        LockedCopy copy = tx.execute(status -> api.findCopyByIdForUpdate(copyId).orElseThrow());

        assertThat(copy).isEqualTo(new LockedCopy(copyId, "Lockable Book", true));
    }

    @Test
    void unknownCopyIsEmpty() {
        Optional<LockedCopy> copy = tx.execute(status -> api.findCopyByIdForUpdate(Long.MAX_VALUE));

        assertThat(copy).isEmpty();
    }

    @Test
    void D9_18_lockedCopyBlocksOtherTransactions() throws Exception {
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CompletableFuture<Void> holder = CompletableFuture.runAsync(() -> tx.executeWithoutResult(status -> {
            api.findCopyByIdForUpdate(copyId);
            locked.countDown();
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }));
        try {
            assertThat(locked.await(10, TimeUnit.SECONDS)).isTrue();

            assertThatThrownBy(() -> jdbc.queryForList("SELECT id FROM copies WHERE id = ? FOR UPDATE NOWAIT", copyId))
                    .hasMessageContaining("55P03");
        } finally {
            release.countDown();
            holder.get(10, TimeUnit.SECONDS);
        }
    }

    @Test
    void P10_marksCopyLoanedAndAvailable() {
        tx.executeWithoutResult(status -> {
            api.findCopyByIdForUpdate(copyId);
            api.markLoaned(copyId);
        });
        assertThat(copyStatus()).isEqualTo("LOANED");
        LockedCopy loaned = tx.execute(status -> api.findCopyByIdForUpdate(copyId).orElseThrow());
        assertThat(loaned.available()).isFalse();

        tx.executeWithoutResult(status -> api.markAvailable(copyId));
        assertThat(copyStatus()).isEqualTo("AVAILABLE");
    }

    @Test
    void statusChangeRollsBackWithTheCallerTransaction() {
        tx.executeWithoutResult(status -> {
            api.markLoaned(copyId);
            status.setRollbackOnly();
        });

        assertThat(copyStatus()).isEqualTo("AVAILABLE");
    }

    @Test
    void requiresTheCallerTransaction() {
        assertThatThrownBy(() -> api.findCopyByIdForUpdate(copyId))
                .isInstanceOf(IllegalTransactionStateException.class);
        assertThatThrownBy(() -> api.markLoaned(copyId))
                .isInstanceOf(IllegalTransactionStateException.class);
    }

    private String copyStatus() {
        return jdbc.queryForObject("SELECT status FROM copies WHERE id = ?", String.class, copyId);
    }
}
