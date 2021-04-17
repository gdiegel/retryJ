package io.github.gdiegel.retry;

import io.github.gdiegel.exception.RetriesExhaustedException;
import io.github.gdiegel.exception.RetryException;
import org.junit.jupiter.api.Test;

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
    void canRetryZeroTimes() {
        final InvocationCounter invocationCounter = new InvocationCounter();
        final var retries = 0;
        final var retry = Retry.<Integer>builder()
                .silently()
                .withRetries(retries)
                .retryOn(shouldRetry -> true).build();
        assertThat(retry.call(invocationCounter::invoke)).as("One original invocation only").isEqualTo(1);
    }

    @Test
    void canRetryOnce() {
        final InvocationCounter invocationCounter = new InvocationCounter();
        final var retries = 1;
        final var retry = Retry.<Integer>builder()
                .silently()
                .withRetries(retries)
                .retryOn(shouldRetry -> true).build();
        assertThat(retry.call(invocationCounter::invoke)).as("One original invocation and one retry").isEqualTo(2);
    }

    @Test
    void canRetryTwice() {
        final InvocationCounter invocationCounter = new InvocationCounter();
        final var retries = 2;
        final var retry = Retry.<Integer>builder()
                .silently()
                .withRetries(retries)
                .retryOn(shouldRetry -> true).build();
        assertThat(retry.call(invocationCounter::invoke)).as("One original invocation and two retries").isEqualTo(3);
    }

    @Test
    void canRetryUntilPredicateEvaluatesToTrue() {
        final var retry = Retry.<Double>builder()
                .withInterval(100, NANOS)
                .withTimeout(1, MINUTES)
                .retryOnException(e -> e.getClass().equals(NumberFormatException.class))
                .retryUntil(d -> d <= 0.01).build();
        assertThat(retry.call(Math::random)).isLessThan(0.01);
    }

    @Test
    void canRetryOnPredicateEvalutatingToTrue() {
        final var sp = new StringProvider();
        final var retry = Retry.<Character>builder()
                .withTimeout(10, SECONDS)
                .retryOn(c -> !c.equals('d')).build();
        final var call = retry.call(sp::getNextChar);
        assertThat(call).isEqualTo('d');
    }

    @Test
    void canRetryOnException() {
        final var tots = new ThrowOnceThenSucceed();
        final var retry = Retry.<String>builder()
                .withTimeout(10, SECONDS)
                .withRetries(2)
                .retryOnException(e -> Objects.equals(e.getClass(), RuntimeException.class)).build();
        assertThat(retry.call(tots::invoke)).isEqualTo("Yippie!");
    }

    @SuppressWarnings({"divzero", "NumericOverflow"})
    @Test
    void shouldThrowRetryExceptionOnUnexpectedException() {
        final var retry = Retry.<Integer>builder()
                .withTimeout(10, SECONDS)
                .retryOnException(e -> e.getClass().equals(RuntimeException.class)).build();
        final Callable<Integer> causesException = () -> 1 / 0;
        assertThatThrownBy(() -> retry.call(causesException)).isExactlyInstanceOf(RetryException.class)
                .hasCauseInstanceOf(ArithmeticException.class);
    }

    @Test
    void shouldThrowRetriesExhaustedExceptionWhenTimeUp() {
        final var retry = Retry.<Integer>builder()
                .withRetries(600)
                .withTimeout(1, SECONDS)
                .retryUntil(o -> false).build();
        assertThatThrownBy(() -> retry.call(() -> 1)).isExactlyInstanceOf(RetriesExhaustedException.class);
    }

    @Test
    void mustNotThrowRetriesExhaustedExceptionWhenSilencedAndTimeUp() {
        final var retry = Retry.<Integer>builder()
                .silently()
                .withRetries(600)
                .withTimeout(1, SECONDS)
                .retryUntil(o -> false).build();
        assertThatCode(() -> retry.call(() -> 1)).doesNotThrowAnyException();

    }

    @Test
    void shouldThrowRetriesExhaustedExceptionWhenRetriesUp() {
        final var retry = Retry.<Integer>builder()
                .withInterval(10, MILLIS)
                .withRetries(10)
                .withTimeout(60, SECONDS)
                .retryUntil(o -> false).build();
        assertThatThrownBy(() -> retry.call(() -> 1)).isExactlyInstanceOf(RetriesExhaustedException.class);
    }

    @Test
    void mustNotThrowRetriesExhaustedExceptionWhenSilencedAndRetriesUp() {
        final var retry = Retry.<Integer>builder()
                .silently()
                .withInterval(10, MILLIS)
                .withRetries(10)
                .withTimeout(60, SECONDS)
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
        int invocations = 0;

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