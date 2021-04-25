package io.github.gdiegel.retry;

import io.github.gdiegel.exception.RetriesExhaustedException;
import io.github.gdiegel.exception.RetryException;

import java.time.LocalTime;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static java.time.LocalTime.now;

/**
 * Default implementation of {@link Retry}. An instance of {@link DefaultRetry} allows executing a {@link Callable} as many
 * times as necessary until either a timeout occurs, the configured number of executions is exhausted,
 * a not previously allowed exception is thrown or a stop condition is satisfied.
 *
 * @param <RESULT> the type of the result of the computation
 * @author Gabriel Diegel
 */
public final class DefaultRetry<RESULT> implements Retry<RESULT> {

    private static final String RETRIES_OR_EXECUTIONS_EXHAUSTED = "Retries or executions exhausted";

    private final RetryPolicy<RESULT> retryPolicy;

    private final AtomicLong currentExecutions = new AtomicLong(0);
    private LocalTime startTime;

    DefaultRetry(RetryPolicy<RESULT> retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    @Override
    public Optional<RESULT> call(Callable<RESULT> callable) {
        Optional<RESULT> result = Optional.empty();
        if (retryPolicy.getMaximumExecutions() == 0) {
            return result;
        }
        setStartTime();
        do {
            result = doCall(callable);
            sleep();
            if (result.isPresent() && retryPolicy.getStopCondition().test(result.get())) {
                break;
            }
        } while (!exhausted());
        return result;
    }

    private Optional<RESULT> doCall(Callable<RESULT> callable) {
        Optional<RESULT> call = Optional.empty();
        try {
            currentExecutions.incrementAndGet();
            call = Optional.of(callable.call());
        } catch (Exception e) {
            if (!retryPolicy.getIgnorableException().test(e)) {
                throw new RetryException(e);
            }
        }
        return call;
    }

    private boolean exhausted() {
        final boolean exhausted = executionsExhausted() || timeExhausted();
        if (exhausted && retryPolicy.isThrowing()) {
            throw new RetriesExhaustedException(RETRIES_OR_EXECUTIONS_EXHAUSTED);
        }
        return exhausted;
    }

    private boolean timeExhausted() {
        return now().isAfter(startTime.plus(retryPolicy.getTimeout()));
    }

    private boolean executionsExhausted() {
        if (retryPolicy.getMaximumExecutions() <= 0) {
            return false;
        }
        return currentExecutions.get() == retryPolicy.getMaximumExecutions();
    }

    private void setStartTime() {
        if (startTime == null) {
            startTime = now();
        }
    }

    private void sleep() {
        try {
            TimeUnit.NANOSECONDS.sleep(retryPolicy.getInterval().toNanos());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DefaultRetry.class.getSimpleName() + "[", "]")
                .add("retryPolicy=" + retryPolicy)
                .add("currentExecutions=" + currentExecutions.get())
                .add("startTime=" + startTime)
                .toString();
    }
}
