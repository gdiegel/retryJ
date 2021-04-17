package io.github.gdiegel.retry;

import io.github.gdiegel.exception.RetriesExhaustedException;
import io.github.gdiegel.exception.RetryException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;

import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.NANOS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

class RetryIT {

    @Test
    void shouldBeAbleToRetryZeroTimes() {
        final InvocationCounter invocationCounter = new InvocationCounter();
        final var retries = 0;
        final var retry = Retry.<Integer>builder()
                .silently()
                .withMaxRetries(retries)
                .retryUntil(shouldStop -> false).build();
        assertThat(retry.call(invocationCounter::invoke)).as("One original invocation only").isEqualTo(1);
    }

    @Test
    void shouldBeAbleToRetryOnce() {
        final InvocationCounter invocationCounter = new InvocationCounter();
        final var retries = 1;
        final var retry = Retry.<Integer>builder()
                .silently()
                .withMaxRetries(retries)
                .retryUntil(shouldStop -> false).build();
        assertThat(retry.call(invocationCounter::invoke)).as("One original invocation and one retry").isEqualTo(2);
    }

    @Test
    void shouldBeAbleToRetryTwice() {
        final InvocationCounter invocationCounter = new InvocationCounter();
        final var retries = 2;
        final var retry = Retry.<Integer>builder()
                .silently()
                .withMaxRetries(retries)
                .retryUntil(shouldStop -> false).build();
        assertThat(retry.call(invocationCounter::invoke)).as("One original invocation and two retries").isEqualTo(3);
    }

    @Test
    void shouldBeAbleToRetryUntilStopConditionEvaluatesToTrue() {
        final var retry = Retry.<Double>builder()
                .withInterval(Duration.of(100, NANOS))
                .withTimeout(Duration.of(1, MINUTES))
                .retryWhenException(e -> e.getClass().equals(NumberFormatException.class))
                .retryUntil(d -> d <= 0.01).build();
        assertThat(retry.call(Math::random)).isLessThan(0.01);
    }

    @Test
    void shouldBeAbleToRetryOnPredicateEvaluatingToTrue() {
        final var sp = new StringProvider();
        final var retry = Retry.<Character>builder()
                .retryUntil(c -> c.equals('d')).build();
        assertThat(retry.call(sp::getNextChar))
                .as("Will retry the iteration through the string until the character d is reached")
                .isEqualTo('d');
    }

    @Test
    void shouldBeAbleToRetryWhenIgnorableExceptionIsThrown() {
        final var tots = new ThrowOnceThenSucceed();
        final var retry = Retry.<String>builder()
                .withTimeout(Duration.of(10, SECONDS))
                .withMaxRetries(2)
                .retryUntil(a -> true)
                .retryWhenException(e -> Objects.equals(e.getClass(), RuntimeException.class)).build();
        assertThat(retry.call(tots::invoke)).isEqualTo("Yippie!");
    }

    @SuppressWarnings({"divzero", "NumericOverflow"})
    @Test
    void shouldThrowRetryExceptionOnNonIgnorableException() {
        final var retry = Retry.<Integer>builder()
                .withTimeout(Duration.of(10, SECONDS))
                .retryWhenException(e -> e.getClass().equals(RuntimeException.class)).build();
        final Callable<Integer> causesException = () -> 1 / 0;
        assertThatThrownBy(() -> retry.call(causesException)).isExactlyInstanceOf(RetryException.class)
                .hasCauseInstanceOf(ArithmeticException.class);
    }

    @Test
    void shouldThrowRetriesExhaustedExceptionWhenTimeoutReached() {
        final var retry = Retry.<Integer>builder()
                .withMaxRetries(600)
                .withTimeout(Duration.of(1, SECONDS))
                .retryUntil(o -> false).build();
        assertThatThrownBy(() -> retry.call(() -> 1)).isExactlyInstanceOf(RetriesExhaustedException.class);
    }

    @Test
    void shouldThrowRetriesExhaustedExceptionWhenRetriesUp() {
        final var retry = Retry.<Integer>builder()
                .withInterval(Duration.of(10, MILLIS))
                .withMaxRetries(10)
                .withTimeout(Duration.of(60, SECONDS))
                .retryUntil(o -> false).build();
        assertThatThrownBy(() -> retry.call(() -> 1)).isExactlyInstanceOf(RetriesExhaustedException.class);
    }

    @Test
    void shouldNotThrowRetriesExhaustedExceptionWhenSilencedAndTimeoutReached() {
        final var retry = Retry.<Integer>builder()
                .silently()
                .withMaxRetries(600)
                .withTimeout(Duration.of(1, SECONDS))
                .retryUntil(o -> false).build();
        assertThatCode(() -> retry.call(() -> 1)).doesNotThrowAnyException();
    }

    @Test
    void shouldNotThrowRetriesExhaustedExceptionWhenSilencedAndRetriesUp() {
        final var retry = Retry.<Integer>builder()
                .silently()
                .withInterval(Duration.of(10, MILLIS))
                .withMaxRetries(10)
                .withTimeout(Duration.of(60, SECONDS))
                .retryUntil(o -> false).build();
        assertThatCode(() -> retry.call(() -> 1)).doesNotThrowAnyException();
    }

    private static class ThrowOnceThenSucceed {
        private boolean thrown = false;

        String invoke() {
            if (thrown) {
                return "Yippie!";
            } else {
                thrown = true;
                throw new RuntimeException("Pow!");
            }
        }
    }

    private static class InvocationCounter {
        private int invocations = 0;

        int invoke() {
            invocations++;
            return invocations;
        }
    }

    private static class StringProvider {
        private static final String START = "abcdef";
        private int pos = 0;

        char getNextChar() {
            return START.toCharArray()[pos++];
        }
    }
}