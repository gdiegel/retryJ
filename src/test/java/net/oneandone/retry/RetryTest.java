package net.oneandone.retry;

import net.oneandone.exception.RetriesExhaustedException;
import net.oneandone.exception.RetryException;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by gdiegel on 6/23/16.
 */
public class RetryTest {

    @Test
    public void canRetryOnce() {
        Integer i = 0;
        final Retry<Integer> retry = Retry.<Integer>builder().withRetries(1).build();
        assertThat(retry.call(() -> i + 1)).isEqualTo(1);
    }

    @Test
    public void canRetryUntilPredicateEvaluatesToTrue() {
        final Retry<Double> retry = Retry.<Double>builder()
                .withInterval(100, ChronoUnit.NANOS)
                .withTimeout(1, ChronoUnit.MINUTES)
                .retryOnException(e -> e.getClass().equals(NumberFormatException.class))
                .retryUntil(d -> d <= 0.01).build();
        assertThat(retry.call(Math::random)).isLessThan(0.01);
    }

    @Test
    public void canRetryOnPredicateEvaluatingToTrue() {
        StringProvider sp = new StringProvider();
        final Retry<Character> retry = Retry.<Character>builder()
                .withTimeout(10, ChronoUnit.SECONDS)
                .retryOn(c -> !c.equals('d')).build();
        final Character call = retry.call(sp::getNextChar);
        assertThat(call).isEqualTo('d');
    }

    @Test
    public void canRetryOnException() {
        final ThrowOnceThenSucceed tots = new ThrowOnceThenSucceed();
        final Retry<String> retry = Retry.<String>builder()
                .withTimeout(10, ChronoUnit.SECONDS)
                .withRetries(2)
                .retryOnException(e -> e.getClass().equals(RuntimeException.class)).build();
        assertThat(retry.call(tots::invoke)).isEqualTo("Yippie!");
    }

    @Test
    public void shouldThrowRetryExceptionOnUnexpectedException() {
        final Retry<Integer> retry = Retry.<Integer>builder()
                .withTimeout(10, ChronoUnit.SECONDS)
                .retryOnException(e -> e.getClass().equals(RuntimeException.class)).build();
        final Callable<Integer> causesException = () -> 1 / 0;
        assertThatThrownBy(() -> retry.call(causesException)).isExactlyInstanceOf(RetryException.class)
                .hasCauseInstanceOf(ArithmeticException.class);
    }

    @Test
    public void shouldThrowRetriesExhaustedExceptionWhenTimeUp() {
        final Retry<Integer> retry = Retry.<Integer>builder()
                .withRetries(600)
                .withTimeout(1, ChronoUnit.SECONDS)
                .retryUntil(o -> 1 == 2).build();
        assertThatThrownBy(() -> retry.call(() -> 1)).isExactlyInstanceOf(RetriesExhaustedException.class);
    }

    @Test
    public void mustNotThrowRetriesExhaustedExceptionWhenSilencedAndTimeUp() {
        final Retry<Integer> retry = Retry.<Integer>builder()
                .silently()
                .withRetries(600)
                .withTimeout(1, ChronoUnit.SECONDS)
                .retryUntil(o -> 1 == 2).build();
        retry.call(() -> 1);
    }

    @Test
    public void shouldThrowRetriesExhaustedExceptionWhenRetriesUp() {
        final Retry<Integer> retry = Retry.<Integer>builder()
                .withInterval(10, ChronoUnit.MILLIS)
                .withRetries(10)
                .withTimeout(60, ChronoUnit.SECONDS)
                .retryUntil(o -> 1 == 2).build();
        assertThatThrownBy(() -> retry.call(() -> 1)).isExactlyInstanceOf(RetriesExhaustedException.class);
    }

    @Test
    public void mustNotThrowRetriesExhaustedExceptionWhenSilencedAndRetriesUp() {
        final Retry<Integer> retry = Retry.<Integer>builder()
                .silently()
                .withInterval(10, ChronoUnit.MILLIS)
                .withRetries(10)
                .withTimeout(60, ChronoUnit.SECONDS)
                .retryUntil(o -> 1 == 2).build();
        retry.call(() -> 1);
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