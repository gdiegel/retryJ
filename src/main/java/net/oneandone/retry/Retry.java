package net.oneandone.retry;

import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

/**
 * Created by gdiegel on 6/14/16.
 *
 * Thanks to Ray Holder, Jean-Baptiste Nizet
 */
public interface Retry<T> {

    Duration getInterval();

    Duration getTimeout();

    Predicate<Exception> getThrowCondition();

    Predicate<T> getRetryCondition();

    long getRetries();

    boolean isSilent();

    long getLeft();

    LocalTime getStartTime();

    static <T> RetryBuilder<T> builder() {
        return new RetryBuilder<>();
    }

    /**
     * Retry the execution wrapped in {@link Callable} when the specified condition matched.
     *
     * @param retryableTask the retryable task
     * @return the return value of the callable
     */
    T call(Callable<T> retryableTask);
}