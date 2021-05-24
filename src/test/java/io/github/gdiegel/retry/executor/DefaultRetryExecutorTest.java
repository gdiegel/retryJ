package io.github.gdiegel.retry.executor;

import io.github.gdiegel.retry.BaseTest;
import io.github.gdiegel.retry.collaborators.InvocationCounter;
import io.github.gdiegel.retry.policy.RetryPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultRetryExecutorTest extends BaseTest {

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void shouldExecuteZeroTimesIfMaximumExecutionsIsZero(int maximumExecutions) {
        final var retryPolicy = RetryPolicy.<Integer>builder().withMaximumExecutions(maximumExecutions).build();
        final var retryExecutor = new DefaultRetryExecutor<>(retryPolicy);
        retryExecutor.execute(IDEMPOTENT_CALLABLE);
        assertThat(retryExecutor.getCurrentExecutions()).isEqualTo(maximumExecutions);
    }

    @Test
    void shouldHoldLastComputedValueWhenExhaustionIsReached() {
        final var invocationCounter = new InvocationCounter();
        final var retryPolicy = RetryPolicy.<Long>builder().withTimeout(Duration.ofSeconds(5)).throwing(false).build();
        final var retryExecutor = new DefaultRetryExecutor<>(retryPolicy);
        final Optional<Long> result = retryExecutor.execute(invocationCounter::invoke);
        assertThat(result).hasValueSatisfying(invocations -> assertThat(invocations).isPositive());
    }
}