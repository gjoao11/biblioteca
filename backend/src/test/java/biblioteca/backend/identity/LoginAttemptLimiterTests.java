package biblioteca.backend.identity;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class LoginAttemptLimiterTests {
    private final MutableClock clock = new MutableClock(Instant.parse("2026-11-22T12:00:00Z"));
    private final LoginAttemptLimiter limiter = new LoginAttemptLimiter(clock);

    @Test
    void blocksAfterTooManyFailuresUntilTheBlockEnds() {
        failTimes(LoginAttemptLimiter.MAX_FAILURES - 1);
        assertThat(limiter.isBlocked("maria", "10.0.0.1")).isFalse();

        failTimes(1);
        assertThat(limiter.isBlocked("maria", "10.0.0.1")).isTrue();

        clock.advance(LoginAttemptLimiter.BLOCK_DURATION);
        assertThat(limiter.isBlocked("maria", "10.0.0.1")).isFalse();
    }

    @Test
    void successResetsTheCount() {
        failTimes(LoginAttemptLimiter.MAX_FAILURES - 1);
        limiter.recordSuccess("maria", "10.0.0.1");
        failTimes(LoginAttemptLimiter.MAX_FAILURES - 1);

        assertThat(limiter.isBlocked("maria", "10.0.0.1")).isFalse();
    }

    @Test
    void otherIpsAndUsersAreNotAffected() {
        failTimes(LoginAttemptLimiter.MAX_FAILURES);

        assertThat(limiter.isBlocked("maria", "10.0.0.2")).isFalse();
        assertThat(limiter.isBlocked("joao", "10.0.0.1")).isFalse();
        assertThat(limiter.isBlocked(" MARIA ", "10.0.0.1")).isTrue();
    }

    private void failTimes(int times) {
        for (int i = 0; i < times; i++) {
            limiter.recordFailure("maria", "10.0.0.1");
        }
    }

    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
