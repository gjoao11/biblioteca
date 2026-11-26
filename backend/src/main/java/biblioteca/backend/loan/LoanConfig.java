package biblioteca.backend.loan;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
class LoanConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
