package io.github.gdiegel.retry;

import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * An instance of {@link Retry} allows retrying a {@link Callable} invocation as many
 * times as necessary until a timeout occurs, the configured number of retries is exhausted,
 * a not previously allowed exception occurs or a stop condition is satisfied.
 *
 * Thanks to Ray Holder, Jean-Baptiste Nizet for the inspiration.
 */
public interface Retry<RESULT> {

    static <RESULT> Retry<RESULT> with(RetryPolicy<RESULT> retryPolicy) {
        return new DefaultRetry<>(retryPolicy);
    }

    /**
     * Retry the execution wrapped in {@link Callable}.
     *
     * @param retryableTask the retryable task
     * @return the return value of the callable
     */
    Optional<RESULT> call(Callable<RESULT> retryableTask);
}