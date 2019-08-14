package net.oneandone.retry;

import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.NANOS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import java.util.concurrent.Callable;

import org.junit.jupiter.api.Test;

import net.oneandone.exception.RetriesExhaustedException;
import net.oneandone.exception.RetryException;

class RetryIT {

    @Test
    void canRetryOnce() {
        final int i = 0;
        final Retry<Integer> retry = Retry.<Integer>builder().withRetries(1).build();
        assertThat(retry.call(() -> i + 1)).isEqualTo(1);
    }

    @Test
    void canRetryUntilPredicateEvaluatesToTrue() {
        final Retry<Double> retry = Retry.<Double>builder()
                .withInterval(100, NANOS)
                .withTimeout(1, MINUTES)
                .retryOnException(e -> e.getClass().equals(NumberFormatException.class))
                .retryUntil(d -> d <= 0.01).build();
        assertThat(retry.call(Math::random)).isLessThan(0.01);
    }

    @Test
    void canRetryOnPredicateEvalutatingToTrue() {
        final StringProvider sp = new StringProvider();
        final Retry<Character> retry = Retry.<Character>builder()
                .withTimeout(10, SECONDS)
                .retryOn(c -> !c.equals('d')).build();
        final Character call = retry.call(sp::getNextChar);
        assertThat(call).isEqualTo('d');
    }

    @Test
    void canRetryOnException() {
        final ThrowOnceThenSucceed tots = new ThrowOnceThenSucceed();
        final Retry<String> retry = Retry.<String>builder()
                .withTimeout(10, SECONDS)
                .withRetries(2)
                .retryOnException(e -> e.getClass().equals(RuntimeException.class)).build();
        assertThat(retry.call(tots::invoke)).isEqualTo("Yippie!");
    }

    @SuppressWarnings({"divzero", "NumericOverflow"})
    @Test
    void shouldThrowRetryExceptionOnUnexpectedException() {
        final Retry<Integer> retry = Retry.<Integer>builder()
                .withTimeout(10, SECONDS)
                .retryOnException(e -> e.getClass().equals(RuntimeException.class)).build();
        final Callable<Integer> causesException = () -> 1 / 0;
        assertThatThrownBy(() -> retry.call(causesException)).isExactlyInstanceOf(RetryException.class)
                .hasCauseInstanceOf(ArithmeticException.class);
    }

    @Test
    void shouldThrowRetriesExhaustedExceptionWhenTimeUp() {
        final Retry<Integer> retry = Retry.<Integer>builder()
                .withRetries(600)
                .withTimeout(1, SECONDS)
                .retryUntil(o -> false).build();
        assertThatThrownBy(() -> retry.call(() -> 1)).isExactlyInstanceOf(RetriesExhaustedException.class);
    }

    @Test
    void mustNotThrowRetriesExhaustedExceptionWhenSilencedAndTimeUp() {
        final Retry<Integer> retry = Retry.<Integer>builder()
                .silently()
                .withRetries(600)
                .withTimeout(1, SECONDS)
                .retryUntil(o -> false).build();
        assertThatCode(() -> retry.call(() -> 1)).doesNotThrowAnyException();

    }

    @Test
    void shouldThrowRetriesExhaustedExceptionWhenRetriesUp() {
        final Retry<Integer> retry = Retry.<Integer>builder()
                .withInterval(10, MILLIS)
                .withRetries(10)
                .withTimeout(60, SECONDS)
                .retryUntil(o -> false).build();
        assertThatThrownBy(() -> retry.call(() -> 1)).isExactlyInstanceOf(RetriesExhaustedException.class);
    }

    @Test
    void mustNotThrowRetriesExhaustedExceptionWhenSilencedAndRetriesUp() {
        final Retry<Integer> retry = Retry.<Integer>builder()
                .silently()
                .withInterval(10, MILLIS)
                .withRetries(10)
                .withTimeout(60, SECONDS)
                .retryUntil(o -> false).build();
        assertThatCode(() -> retry.call(() -> 1)).doesNotThrowAnyException();
    }

    private class ThrowOnceThenSucceed {
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

    private class StringProvider {
        private final String start = "abcdef";
        private int pos = 0;

        char getNextChar() {
            return start.toCharArray()[pos++];
        }
    }
}