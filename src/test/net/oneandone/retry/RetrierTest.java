package net.oneandone.retry;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by gdiegel on 6/23/16.
 */
public class RetrierTest {

    @Test
    public void canRetryOnce() {
        Integer i = 0;
        final Retrier<Integer> retrier = Retrier.<Integer>builder().retryUntil(integer -> integer == 1).build();
        final Integer result = retrier.call(() -> i + 1);
        assertThat(result).isEqualTo(1);
    }

}