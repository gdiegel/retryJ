package io.github.gdiegel.retry.executor;

import io.github.gdiegel.retry.BaseTest;
import io.github.gdiegel.retry.ThrowImmediately;
import io.github.gdiegel.retry.exception.RetriesExhaustedException;
import io.github.gdiegel.retry.exception.RetryException;
import io.github.gdiegel.retry.policy.RetryPolicy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultRetryExecutorTest extends BaseTest {

    @Test
    void shouldReturnEmptyOptionalIfMaximumExecutionsIsZero() {
        final var retryPolicy = RetryPolicy.<Integer>builder().withMaximumExecutions(0).build();
        final var retryExecutor = new DefaultRetryExecutor<>(retryPolicy);
        final var result = retryExecutor.execute(IDEMPOTENT_CALLABLE);
        assertThat(result).isEmpty();
        assertThat(retryExecutor.getCurrentExecutions()).isZero();
    }

    @Test
    void shouldThrowRetryExceptionIfThrownExceptionIsNotIgnorable() throws StackOverflowError {
        final var retryPolicy = RetryPolicy.<String>builder().build();
        final var retryExecutor = new DefaultRetryExecutor<>(retryPolicy);
        final var ti = new ThrowImmediately();
        assertThatThrownBy(() -> retryExecutor.execute(ti::invoke))
                .isExactlyInstanceOf(RetryException.class)
                .hasCauseInstanceOf(RuntimeException.class);
        assertThat(retryExecutor.getCurrentExecutions()).isOne();
    }

    @Test
    void shouldThrowRetriesExhaustedExceptionIfExceptionsAreUpAndThrowing() throws StackOverflowError {
        final var retryPolicy = RetryPolicy.<Integer>builder().withMaximumExecutions(1L).throwing(true).build();
        final var retryExecutor = new DefaultRetryExecutor<>(retryPolicy);
        assertThatThrownBy(() -> retryExecutor.execute(IDEMPOTENT_CALLABLE))
                .isExactlyInstanceOf(RetriesExhaustedException.class);
        assertThat(retryExecutor.getCurrentExecutions()).isEqualTo(1L);
    }
}