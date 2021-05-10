package io.github.gdiegel.retry;

import com.google.common.annotations.VisibleForTesting;
import io.github.gdiegel.retry.exception.RetriesExhaustedException;
import io.github.gdiegel.retry.exception.RetryException;
import io.github.gdiegel.retry.policy.RetryPolicy;

import java.time.LocalTime;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import static java.time.LocalTime.now;

class RetryExecutor<RESULT> {

    private static final String RETRIES_OR_EXECUTIONS_EXHAUSTED = "Retries or executions exhausted";
    private final RetryPolicy<RESULT> retryPolicy;

    private LocalTime startTime;
    private final LongAdder currentExecutions = new LongAdder();

    RetryExecutor(RetryPolicy<RESULT> retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    @VisibleForTesting
    long getCurrentExecutions() {
        return currentExecutions.sum();
    }

    public Optional<RESULT> execute(Callable<RESULT> callable) {
        if (retryPolicy.getMaximumExecutions() == 0) {
            return Optional.empty();
        }
        return doExecute(callable);
    }

    private Optional<RESULT> doExecute(Callable<RESULT> callable) {
        Optional<RESULT> result = Optional.empty();
        setStartTime();
        do {
            try {
                currentExecutions.increment();
                result = Optional.ofNullable(callable.call());
            } catch (Exception e) {
                if (!retryPolicy.getIgnorableException().test(e)) {
                    throw new RetryException(e);
                }
            }
            sleep();
            if (result.isPresent() && retryPolicy.getStopCondition().test(result.get())) {
                break;
            }
        } while (!exhausted());
        return result;
    }

    private boolean exhausted() {
        final boolean exhausted = timeExhausted() || executionsExhausted();
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
        return currentExecutions.sum() == retryPolicy.getMaximumExecutions();
    }

    private void sleep() {
        try {
            TimeUnit.NANOSECONDS.sleep(retryPolicy.getInterval().toNanos());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void setStartTime() {
        if (startTime == null) {
            startTime = now();
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RetryExecutor.class.getSimpleName() + "[", "]")
                .add("retryPolicy=" + retryPolicy)
                .add("startTime=" + startTime)
                .add("currentExecutions=" + currentExecutions)
                .toString();
    }
}