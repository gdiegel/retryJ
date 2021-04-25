package io.github.gdiegel.retry;

import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * {@link Retry} allows executing a {@link Callable<RESULT>} zero or more times and will return an
 * {@link Optional<RESULT>} holding an instance of type {@link RESULT} of the result of the computation
 * or not.
 *
 * Thanks to Ray Holder, Jean-Baptiste Nizet and Jonathan Halterman for the inspiration.
 *
 * @author Gabriel Diegel
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