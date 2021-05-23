package io.github.gdiegel.retry.executor;

import io.github.gdiegel.retry.BaseTest;
import io.github.gdiegel.retry.policy.RetryPolicy;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
}