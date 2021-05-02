package io.github.gdiegel.retry.policy;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class RetryPolicyTest {

    private static final Duration INTERVAL = Duration.ofSeconds(1);
    private static final Duration TIMEOUT = Duration.ofDays(1);
    private static final long MAXIMUM_EXECUTIONS = 1000L;
    private static final Predicate<Exception> IGNORABLE_EXCEPTION = e -> e.getCause().getClass() == StackOverflowError.class;
    private static final Predicate<Integer> STOP_CONDITION = integer -> integer > 5;
    private static final boolean THROWING = true;

    @Test
    void canCreateBuilderFromRetryPolicy() {
        final RetryPolicyBuilder<Integer> builder = RetryPolicy.builder();
        assertThat(builder).isNotNull();
    }

    @Test
    void canCreateInstanceOfRetryPolicyViaConstructor() {
        final RetryPolicy<Integer> retryPolicy = new RetryPolicy<>(INTERVAL, TIMEOUT, MAXIMUM_EXECUTIONS, IGNORABLE_EXCEPTION, STOP_CONDITION, THROWING);
        assertThat(retryPolicy).isNotNull();
        assertThat(retryPolicy.getInterval()).isEqualTo(INTERVAL);
        assertThat(retryPolicy.getTimeout()).isEqualTo(TIMEOUT);
        assertThat(retryPolicy.getMaximumExecutions()).isEqualTo(MAXIMUM_EXECUTIONS);
        assertThat(retryPolicy.getIgnorableException()).isEqualTo(IGNORABLE_EXCEPTION);
        assertThat(retryPolicy.getStopCondition()).isEqualTo(STOP_CONDITION);
        assertThat(retryPolicy.isThrowing()).isEqualTo(THROWING);
    }

    @Test
    void canCreateInstanceOfRetryPolicyViaBuilder() {
        final RetryPolicyBuilder<Integer> builder = RetryPolicy.builder();
        final RetryPolicy<Integer> retryPolicy = builder.withInterval(INTERVAL)
                .withTimeout(TIMEOUT)
                .withMaximumExecutions(MAXIMUM_EXECUTIONS)
                .ignoreWhen(IGNORABLE_EXCEPTION)
                .retryUntil(STOP_CONDITION)
                .throwing(true)
                .build();
        assertThat(retryPolicy).isNotNull();
        assertThat(retryPolicy.getInterval()).isEqualTo(INTERVAL);
        assertThat(retryPolicy.getTimeout()).isEqualTo(TIMEOUT);
        assertThat(retryPolicy.getMaximumExecutions()).isEqualTo(MAXIMUM_EXECUTIONS);
        assertThat(retryPolicy.getIgnorableException()).isEqualTo(IGNORABLE_EXCEPTION);
        assertThat(retryPolicy.getStopCondition()).isEqualTo(STOP_CONDITION);
        assertThat(retryPolicy.isThrowing()).isEqualTo(THROWING);
    }
}