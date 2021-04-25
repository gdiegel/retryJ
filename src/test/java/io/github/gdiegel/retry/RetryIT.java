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

import io.github.gdiegel.retry.exception.RetriesExhaustedException;
import io.github.gdiegel.retry.exception.RetryException;
import io.github.gdiegel.retry.policy.RetryPolicy;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.NANOS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

class RetryIT {

    @Test
    void shouldReturnEmptyOptionalWhenZeroExecutionsAreConfigured() {
        final InvocationCounter invocationCounter = new InvocationCounter();
        final var retryPolicy = RetryPolicy.<Long>builder()
                .withMaxExecutions(0L)
                .build();
        assertThat(Retry.with(retryPolicy).call(invocationCounter::invoke))
                .as("Callable was invoked zero times")
                .isEmpty();
    }

    @Test
    void shouldReturnResultWhenOneExecutionIsConfigured() {
        final InvocationCounter invocationCounter = new InvocationCounter();
        final var executions = 1L;
        final var retryPolicy = RetryPolicy.<Long>builder()
                .withMaxExecutions(executions)
                .build();
        assertThat(Retry.with(retryPolicy).call(invocationCounter::invoke))
                .as("One original invocation")
                .contains(executions);
    }

    @Test
    void shouldBeAbleToExecuteTwice() {
        final InvocationCounter invocationCounter = new InvocationCounter();
        final var executions = 2L;
        final var retryPolicy = RetryPolicy.<Long>builder()
                .withMaxExecutions(executions)
                .build();
        assertThat(Retry.with(retryPolicy).call(invocationCounter::invoke))
                .as("Two original invocations")
                .contains(executions);
    }

    @Test
    void shouldBeAbleToExecuteManyTimes() {
        final InvocationCounter invocationCounter = new InvocationCounter();
        final var executions = 100000L;
        final var retryPolicy = RetryPolicy.<Long>builder()
                .withMaxExecutions(executions)
                .withInterval(Duration.ZERO)
                .withTimeout(Duration.ofHours(1))
                .retryUntil(shouldStop -> false).build();
        assertThat(Retry.with(retryPolicy).call(invocationCounter::invoke))
                .as("100000 original invocations")
                .contains(executions);
        assertThat(invocationCounter.invocations.get()).isEqualTo(executions);
    }

    @Test
    void shouldBeAbleToRetryUntilStopConditionEvaluatesToTrue() {
        final var retryPolicy = RetryPolicy.<Double>builder()
                .withInterval(Duration.of(100, NANOS))
                .withTimeout(Duration.of(1, MINUTES))
                .retryWhenException(e -> e.getClass().equals(NumberFormatException.class))
                .retryUntil(d -> d <= 0.01)
                .build();
        assertThat(Retry.with(retryPolicy).call(Math::random))
                .isPresent()
                .get(InstanceOfAssertFactories.DOUBLE).isLessThan(0.01);
    }

    @Test
    void shouldBeAbleToRetryOnPredicateEvaluatingToTrue() {
        final var sp = new StringProvider();
        final var retryPolicy = RetryPolicy.<Character>builder()
                .retryUntil(c -> c.equals('d'))
                .build();
        assertThat(Retry.with(retryPolicy).call(sp::getNextChar))
                .as("Will retry the iteration through the string until the character 'd' is reached")
                .isPresent()
                .get(InstanceOfAssertFactories.CHARACTER).isEqualTo('d');
    }

    @Test
    void shouldBeAbleToRetryWhenIgnorableExceptionIsThrown() {
        final var tots = new ThrowOnceThenSucceed();
        final var retryPolicy = RetryPolicy.<String>builder()
                .withMaxExecutions(2)
                .retryWhenException(e -> Objects.equals(e.getClass(), RuntimeException.class)).build();
        assertThat(Retry.with(retryPolicy).call(tots::invoke))
                .isPresent().get(InstanceOfAssertFactories.STRING)
                .isEqualTo("Yippie!");
    }

    @SuppressWarnings({"divzero", "NumericOverflow"})
    @Test
    void shouldThrowRetryExceptionOnNonIgnorableException() {
        final var retryPolicy = RetryPolicy.<Integer>builder()
                .retryWhenException(e -> e.getClass().equals(RuntimeException.class))
                .build();
        final Callable<Integer> causesException = () -> 1 / 0;
        assertThatThrownBy(() -> Retry.with(retryPolicy).call(causesException)).isExactlyInstanceOf(RetryException.class)
                .hasCauseInstanceOf(ArithmeticException.class);
    }

    @Test
    void shouldThrowRetriesExhaustedExceptionWhenTimeoutReached() {
        final var retryPolicy = RetryPolicy.<Integer>builder()
                .withTimeout(Duration.of(1, SECONDS))
                .throwing()
                .build();
        assertThatThrownBy(() -> Retry.with(retryPolicy).call(() -> 1)).isExactlyInstanceOf(RetriesExhaustedException.class);
    }

    @Test
    void shouldThrowRetriesExhaustedExceptionWhenRetriesUp() {
        final var retryPolicy = RetryPolicy.<Integer>builder()
                .withMaxExecutions(10)
                .throwing()
                .build();
        assertThatThrownBy(() -> Retry.with(retryPolicy).call(() -> 1)).isExactlyInstanceOf(RetriesExhaustedException.class);
    }

    @Test
    void shouldNotThrowRetriesExhaustedExceptionWhenSilencedAndTimeoutReached() {
        final var retryPolicy = RetryPolicy.<Integer>builder()
                .withTimeout(Duration.of(1, SECONDS))
                .build();
        assertThatCode(() -> Retry.with(retryPolicy).call(() -> 1)).doesNotThrowAnyException();
    }

    @Test
    void shouldNotThrowRetriesExhaustedExceptionWhenSilencedAndRetriesUp() {
        final var retryPolicy = RetryPolicy.<Integer>builder()
                .withMaxExecutions(10)
                .build();
        assertThatCode(() -> Retry.with(retryPolicy).call(() -> 1)).doesNotThrowAnyException();
    }

    private static class ThrowOnceThenSucceed {
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

    private static class InvocationCounter {
        private final AtomicLong invocations = new AtomicLong(0L);

        long invoke() {
            return invocations.incrementAndGet();
        }
    }

    private static class StringProvider {
        private static final String START = "abcdef";
        private int pos = 0;

        char getNextChar() {
            return START.toCharArray()[pos++];
        }
    }
}