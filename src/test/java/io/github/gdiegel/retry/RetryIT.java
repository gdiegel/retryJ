/*
 *  Copyright 2021 Gabriel Diegel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.github.gdiegel.retry;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.NANOS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import com.google.common.base.Stopwatch;
import io.github.gdiegel.retry.collaborators.InvocationCounter;
import io.github.gdiegel.retry.collaborators.StringProvider;
import io.github.gdiegel.retry.collaborators.ThrowOnceThenSucceed;
import io.github.gdiegel.retry.exception.RetriesExhaustedException;
import io.github.gdiegel.retry.exception.RetryException;
import io.github.gdiegel.retry.executor.RetryExecutor;
import io.github.gdiegel.retry.policy.RetryPolicy;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class RetryIT {

    @Test
    void shouldReturnEmptyOptionalWhenZeroExecutionsAreConfigured() {
        final InvocationCounter invocationCounter = new InvocationCounter();
        final RetryPolicy<Long> retryPolicy = RetryPolicy.<Long>builder()
            .withMaximumExecutions(0L)
            .build();
        final RetryExecutor<Long> retry = Retry.with(retryPolicy);
        final Optional<Long> execute = retry.execute(invocationCounter::invoke);
        assertThat(execute)
            .as("Callable was invoked zero times")
            .isEmpty();
        assertThat(invocationCounter.getInvocations()).isZero();
    }

    @Test
    void shouldReturnResultWhenOneExecutionIsConfigured() {
        final InvocationCounter invocationCounter = new InvocationCounter();
        final long executions = 1L;
        final RetryPolicy<Long> retryPolicy = RetryPolicy.<Long>builder()
            .withMaximumExecutions(executions)
            .build();
        assertThat(Retry.with(retryPolicy).execute(invocationCounter::invoke))
            .as("One original invocation")
            .contains(executions);
        assertThat(invocationCounter.getInvocations()).isEqualTo(executions);
    }

    @Test
    void shouldBeAbleToExecuteTwice() {
        final InvocationCounter invocationCounter = new InvocationCounter();
        final long executions = 2L;
        final RetryPolicy<Long> retryPolicy = RetryPolicy.<Long>builder()
            .withMaximumExecutions(executions)
            .build();
        assertThat(Retry.with(retryPolicy).execute(invocationCounter::invoke))
            .as("Two original invocations")
            .contains(executions);
        assertThat(invocationCounter.getInvocations()).isEqualTo(executions);
    }

    @Test
    void shouldBeAbleToExecuteManyTimes() {
        final InvocationCounter invocationCounter = new InvocationCounter();
        final long executions = 10000L;
        final RetryPolicy<Long> retryPolicy = RetryPolicy.<Long>builder()
            .withMaximumExecutions(executions)
            .withInterval(Duration.ZERO)
            .build();
        final RetryExecutor<Long> retry = Retry.with(retryPolicy);
        final Optional<Long> result = retry.execute(invocationCounter::invoke);
        assertThat(result)
            .as("10000 original invocations")
            .contains(executions);
        assertThat(invocationCounter.getInvocations()).isEqualTo(executions);
    }

    @Test
    void shouldBeAbleToRetryUntilStopConditionEvaluatesToTrue() {
        final RetryPolicy<Double> retryPolicy = RetryPolicy.<Double>builder()
            .withInterval(Duration.of(100, NANOS))
            .withTimeout(Duration.of(1, MINUTES))
            .ignoreWhen(e -> e.getClass().equals(NumberFormatException.class))
            .retryUntil(d -> d <= 0.01)
            .build();
        assertThat(Retry.with(retryPolicy).execute(Math::random))
            .isPresent()
            .get(InstanceOfAssertFactories.DOUBLE).isLessThan(0.01);
    }

    @Test
    void shouldBeAbleToRetryOnPredicateEvaluatingToTrue() {
        final StringProvider sp = new StringProvider();
        final RetryPolicy<Character> retryPolicy = RetryPolicy.<Character>builder()
            .retryUntil(c -> c.equals('d'))
            .build();
        assertThat(Retry.with(retryPolicy).execute(sp::getNextChar))
            .as("Will retry the iteration through the string until the character 'd' is reached")
            .isPresent()
            .get(InstanceOfAssertFactories.CHARACTER).isEqualTo('d');
    }

    @Test
    void shouldBeAbleToRetryWhenIgnorableExceptionIsThrown() {
        final ThrowOnceThenSucceed tots = new ThrowOnceThenSucceed();
        final RetryPolicy<String> retryPolicy = RetryPolicy.<String>builder()
            .withMaximumExecutions(2)
            .ignoreWhen(exception -> exception.getClass() == RuntimeException.class).build();
        assertThat(Retry.with(retryPolicy).execute(tots::invoke))
            .isPresent().get(InstanceOfAssertFactories.STRING)
            .isEqualTo("Yippie!");
    }

    @SuppressWarnings({"divzero", "NumericOverflow"})
    @Test
    void shouldThrowRetryExceptionOnNonIgnorableException() {
        final RetryPolicy<Integer> retryPolicy = RetryPolicy.<Integer>builder()
            .ignoreWhen(e -> e.getClass().equals(RuntimeException.class))
            .build();
        final Callable<Integer> causesException = () -> 1 / 0;
        final RetryExecutor<Integer> retryExecutor = Retry.with(retryPolicy);
        assertThatThrownBy(() -> retryExecutor.execute(causesException))
            .isExactlyInstanceOf(RetryException.class)
            .hasCauseInstanceOf(ArithmeticException.class);
    }

    @Test
    void shouldThrowRetriesExhaustedExceptionWhenTimeoutReached() {
        final RetryPolicy<Integer> retryPolicy = RetryPolicy.<Integer>builder()
            .withInterval(Duration.ZERO)
            .withTimeout(Duration.of(5, SECONDS))
            .throwing(true)
            .build();
        final RetryExecutor<Integer> retry = Retry.with(retryPolicy);
        final Stopwatch started = Stopwatch.createStarted();
        assertThatThrownBy(() -> retry.execute(() -> 1))
            .isExactlyInstanceOf(RetriesExhaustedException.class);
        started.stop();
        assertThat(started.elapsed()).isCloseTo(Duration.ofSeconds(5), Duration.ofMillis(500));
    }

    @Test
    void shouldThrowRetriesExhaustedExceptionWhenRetriesUp() {
        final long executions = 10L;
        final RetryPolicy<Integer> retryPolicy = RetryPolicy.<Integer>builder()
            .withMaximumExecutions(executions)
            .throwing(true)
            .build();
        final RetryExecutor<Integer> retry = Retry.with(retryPolicy);
        assertThatThrownBy(() -> retry.execute(() -> 1))
            .isExactlyInstanceOf(RetriesExhaustedException.class);
    }

    @Test
    void shouldNotThrowRetriesExhaustedExceptionWhenSilencedAndTimeoutReached() {
        final RetryPolicy<Integer> retryPolicy = RetryPolicy.<Integer>builder()
            .withTimeout(Duration.of(1, SECONDS))
            .build();
        assertThatCode(() -> Retry.with(retryPolicy).execute(() -> 1)).doesNotThrowAnyException();
    }

    @Test
    void shouldNotThrowRetriesExhaustedExceptionWhenSilencedAndRetriesUp() {
        final long executions = 10L;
        final RetryPolicy<Integer> retryPolicy = RetryPolicy.<Integer>builder()
            .withMaximumExecutions(executions)
            .build();
        final RetryExecutor<Integer> retry = Retry.with(retryPolicy);
        assertThatCode(() -> retry.execute(() -> 1)).doesNotThrowAnyException();
    }

}