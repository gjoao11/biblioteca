package biblioteca.backend.loan;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.util.Optional;

@Configuration
class LoanConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    @ConditionalOnMissingBean(StudentLoanPort.class)
    StudentLoanPort stubStudentLoanPort() {
        return new StudentLoanPort() {
            @Override
            public Optional<StudentSnapshot> findByIdForUpdate(Long studentId) {
                throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Student module not integrated yet");
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(CatalogLoanPort.class)
    CatalogLoanPort stubCatalogLoanPort() {
        return new CatalogLoanPort() {
            @Override
            public Optional<CopySnapshot> findCopyByIdForUpdate(Long copyId) {
                throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Catalog module not integrated yet");
            }

            @Override
            public void markLoaned(Long copyId) {
                throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Catalog module not integrated yet");
            }

            @Override
            public void markAvailable(Long copyId) {
                throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Catalog module not integrated yet");
            }
        };
    }
}
