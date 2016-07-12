package net.oneandone.retry;

import net.oneandone.exception.RetryException;

import java.util.concurrent.Callable;

/**
 * Created by gdiegel on 6/14/16.
 *
 * Thanks to Ray Holder, Jean-Baptiste Nizet
 */
public interface Retry<T> {

    static <T> RetryBuilder<T> builder() {
        return new RetryBuilder<>();
    }

    /**
     * Retry the execution wrapped in {@link Callable} when the specified condition matched.
     *
     * @param retryableTask the retryable task
     * @return the return value of the callable
     */
    T call(Callable<T> retryableTask) throws RetryException;
}