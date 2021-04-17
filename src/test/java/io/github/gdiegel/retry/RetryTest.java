package io.github.gdiegel.retry;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

class RetryTest {

    @Test
    void canUseRetryBuilderRetries() {
        final Retry builder = Retry.builder().withRetries(1).build();
        assertThat(builder.getRetries()).isEqualTo(1);
    }

    @Test
    void canUseRetryBuilder() {
        final Duration fortyFiveSeconds = Duration.of(45, ChronoUnit.SECONDS);
        final Retry builder = Retry.builder().withInterval(fortyFiveSeconds).build();
        assertThat(builder.getInterval()).isEqualByComparingTo(fortyFiveSeconds);
    }

}