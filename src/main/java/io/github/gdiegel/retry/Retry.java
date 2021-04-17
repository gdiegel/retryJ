package io.github.gdiegel.retry;

import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

/**
 * An instance of {@link Retry} allows retrying a {@link Callable} invocation as many
 * times as necessary until a timeout occurs, the configured number of retries is exhausted,
 * a not previously allowed exception occurs or a stop condition is satisfied.
 *
 * Thanks to Ray Holder, Jean-Baptiste Nizet for the inspiration.
 */
public interface Retry<T> {

    @org.jetbrains.annotations.NotNull
    @org.jetbrains.annotations.Contract(" -> new")
    static <T> RetryBuilder<T> builder() {
        return new RetryBuilder<>();
    }

    Duration getInterval();

    Duration getTimeout();

    Predicate<Exception> getIgnorableException();

    Predicate<T> getStopCondition();

    long getMaxRetries();

    boolean isSilent();

    long getLeft();

    LocalTime getStartTime();

    /**
     * Retry the execution wrapped in {@link Callable} when the specified condition matched.
     *
     * @param retryableTask the retryable task
     * @return the return value of the callable
     */
    T call(Callable<T> retryableTask);
}