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
 * @param <RESULT> the type of the result of the computation
 * @author Gabriel Diegel
 */
public interface Retry<RESULT> {

    /**
     * Set a {@link RetryPolicy<RESULT>} for the computation.
     * @param retryPolicy the retry policy to use when executing the computation
     * @param <RESULT> the type of the result of the computation
     * @return Self for method chaining
     */
    static <RESULT> Retry<RESULT> with(RetryPolicy<RESULT> retryPolicy) {
        return new DefaultRetry<>(retryPolicy);
    }

    /**
     * Execute the computation wrapped in {@link Callable}.
     *
     * @param retryableTask the retryable task
     * @return An {@link Optional} holding the result of the computation or not.
     */
    Optional<RESULT> call(Callable<RESULT> retryableTask);
}