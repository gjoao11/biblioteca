package biblioteca.backend.loan;

import biblioteca.backend.TestCsrf;
import biblioteca.backend.TestFixtures;
import biblioteca.backend.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Testes de concorrência do módulo loan.
 *
 * <p>ATENÇÃO — estes testes estão desabilitados enquanto T3 (catalog) e T4 (student) não
 * estiverem integrados. Quando os adaptadores reais {@code StudentModuleLoanAdapter} e
 * {@code CatalogModuleLoanAdapter} estiverem na {@code main}, remova os {@code @Disabled}
 * e ajuste o setup para criar aluno APPROVED com categoria de max_loans suficiente.
 *
 * <p>Critério de pronto da E2 (D9-18):
 * duas threads tentam emprestar o mesmo exemplar simultaneamente →
 * uma retorna 201 e a outra 409, restando exatamente 1 empréstimo aberto no banco.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class LoanConcurrencyTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    TestFixtures fixtures;
    long librarianId;
    long copyId;
    long studentId1;
    long studentId2;

    @BeforeEach
    void setUp() {
        fixtures = new TestFixtures(jdbc);
        librarianId = fixtures.user();
        long catId = fixtures.bookCategory(TestFixtures.unique("conc-cat"));
        long bookId = fixtures.book(catId, "Concurrency Book", "Author", librarianId);
        copyId = fixtures.copy(bookId, "AVAILABLE");
        // Dois alunos distintos (cada um tenta pegar o mesmo exemplar)
        studentId1 = fixtures.student();
        studentId2 = fixtures.student();
        // TODO (após T4): aprovar ambos os alunos via UPDATE
        // jdbc.update("UPDATE students SET status = 'APPROVED' WHERE id IN (?,?)", studentId1, studentId2);
    }

    /**
     * D9-18: duas threads tentam POST /api/loans com o mesmo copyId ao mesmo tempo.
     * Uma deve receber 201 e a outra 409.
     * O índice único parcial {@code loans_one_open_per_copy_idx} garante a consistência.
     *
     * <p>Este teste será habilitado quando T3 e T4 estiverem integrados, pois requer:
     * - Alunos com status APPROVED (T4)
     * - Lock pessimista real em copies (T3)
     */
    @Test
    @Disabled("Aguarda T3 (CatalogModuleLoanAdapter) e T4 (StudentModuleLoanAdapter) na main")
    @WithMockUser
    void D9_18_concurrentLoanAttempts_oneSucceedsOneConflicts() throws Exception {
        String body1 = String.format("{\"studentId\":%d,\"copyId\":%d}", studentId1, copyId);
        String body2 = String.format("{\"studentId\":%d,\"copyId\":%d}", studentId2, copyId);

        var csrf = TestCsrf.token(mvc);
        CountDownLatch startGate = new CountDownLatch(1);
        List<Future<Integer>> futures = new ArrayList<>();

        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            futures.add(pool.submit(() -> {
                startGate.await();
                MvcResult result = mvc.perform(
                        post("/api/loans")
                                .with(csrf)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body1))
                        .andReturn();
                return result.getResponse().getStatus();
            }));

            futures.add(pool.submit(() -> {
                startGate.await();
                MvcResult result = mvc.perform(
                        post("/api/loans")
                                .with(csrf)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body2))
                        .andReturn();
                return result.getResponse().getStatus();
            }));

            startGate.countDown(); // dispara as duas threads ao mesmo tempo

            List<Integer> statuses = new ArrayList<>();
            for (Future<Integer> f : futures) {
                statuses.add(f.get());
            }

            // Uma deve ter 201, a outra 409
            assertThat(statuses).containsExactlyInAnyOrder(201, 409);
        }

        // Exatamente 1 empréstimo aberto para o exemplar
        Long openCount = jdbc.queryForObject(
                "SELECT count(*) FROM loans WHERE copy_id = ? AND returned_at IS NULL",
                Long.class, copyId);
        assertThat(openCount).isEqualTo(1);
    }
}
