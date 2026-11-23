package biblioteca.backend.identity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// A chave inclui o IP para que ninguém consiga trancar a conta de outra pessoa de propósito.
@Component
class LoginAttemptLimiter {
    static final int MAX_FAILURES = 5;
    static final Duration BLOCK_DURATION = Duration.ofMinutes(15);

    private final Map<String, Failures> failures = new ConcurrentHashMap<>();
    private final Clock clock;

    @Autowired
    LoginAttemptLimiter() {
        this(Clock.systemUTC());
    }

    LoginAttemptLimiter(Clock clock) {
        this.clock = clock;
    }

    boolean isBlocked(String username, String ip) {
        Failures current = failures.get(key(username, ip));
        return current != null && current.count() >= MAX_FAILURES && !current.expired(clock.instant());
    }

    void recordFailure(String username, String ip) {
        Instant now = clock.instant();
        failures.compute(key(username, ip), (key, current) ->
                current == null || current.expired(now)
                        ? new Failures(1, now)
                        : new Failures(current.count() + 1, current.firstFailure()));
        failures.values().removeIf(entry -> entry.expired(now));
    }

    void recordSuccess(String username, String ip) {
        failures.remove(key(username, ip));
    }

    private static String key(String username, String ip) {
        return username.strip().toLowerCase(Locale.ROOT) + "|" + ip;
    }

    private record Failures(int count, Instant firstFailure) {
        boolean expired(Instant now) {
            return !now.isBefore(firstFailure.plus(BLOCK_DURATION));
        }
    }
}
