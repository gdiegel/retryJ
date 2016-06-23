package net.oneandone.retry;

import net.oneandone.exception.RetryException;

import java.util.concurrent.Callable;

/**
 * Created by gdiegel on 6/14/16.
 */
public interface Retrier<T> {

    static <T> RetrierBuilder<T> builder() {
        return new RetrierBuilder<>();
    }

    /**
     * Retry the execution wrapped in {@link Callable} when the specified condition matched.
     *
     * @param retryableTask the retryable task
     * @return the return value of the callable
     */
    T call(Callable<T> retryableTask) throws RetryException;
}