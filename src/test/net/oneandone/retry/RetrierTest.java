package net.oneandone.retry;

import org.junit.Test;

import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by gdiegel on 6/23/16.
 */
public class RetrierTest {

    @Test
    public void canRetryOnce() {
        Integer i = 0;
        final Retrier<Integer> retrier = Retrier.<Integer>builder().withRetries(1).build();
        assertThat(retrier.call(() -> i + 1)).isEqualTo(1);
    }

    @Test
    public void canRetryUntilPredicateIsTrue() {
        final Retrier<Double> retrier = Retrier.<Double>builder()
                .withInterval(100, ChronoUnit.NANOS)
                .withTimeout(10, ChronoUnit.SECONDS)
                .retryOnException(e -> e.getClass().equals(NumberFormatException.class))
                .retryUntil(d -> d <= 0.01).build();
        assertThat(retrier.call(Math::random)).isLessThan(0.01);
    }

}