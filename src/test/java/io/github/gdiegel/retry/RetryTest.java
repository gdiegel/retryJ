package io.github.gdiegel.retry;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class RetryTest {

    @Test
    void canUseRetryBuilderRetries() {
        final var builder = Retry.builder().withMaxRetries(1).build();
        assertThat(builder.getMaxRetries()).isEqualTo(1);
    }

    @Test
    void canUseRetryBuilder() {
        final var fortyFiveSeconds = Duration.of(45, ChronoUnit.SECONDS);
        final var builder = Retry.builder().withInterval(fortyFiveSeconds).build();
        assertThat(builder.getInterval()).isEqualByComparingTo(fortyFiveSeconds);
    }

}