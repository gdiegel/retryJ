package io.github.gdiegel.retry.executor;

import com.google.common.annotations.VisibleForTesting;
import io.github.gdiegel.retry.exception.RetriesExhaustedException;
import io.github.gdiegel.retry.exception.RetryException;
import io.github.gdiegel.retry.policy.RetryPolicy;

import java.time.LocalTime;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import static java.time.LocalTime.now;

public class DefaultRetryExecutor<RESULT> implements RetryExecutor<RESULT> {

    private static final String RETRIES_OR_EXECUTIONS_EXHAUSTED = "Retries or executions exhausted";
    private final RetryPolicy<RESULT> retryPolicy;

    private LocalTime startTime;
    private final LongAdder currentExecutions = new LongAdder();

    public DefaultRetryExecutor(RetryPolicy<RESULT> retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    @VisibleForTesting
    long getCurrentExecutions() {
        return currentExecutions.sum();
    }

    @Override
    public Optional<RESULT> execute(Callable<RESULT> callable) {
        if (retryPolicy.maximumExecutions() == 0) {
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
                if (!retryPolicy.ignorableException().test(e)) {
                    throw new RetryException(e);
                }
            }
            sleep();
            if (result.isPresent() && retryPolicy.stopCondition().test(result.get())) {
                break;
            }
        } while (!exhausted());
        return result;
    }

    private boolean exhausted() {
        final boolean exhausted = timeExhausted() || executionsExhausted();
        if (exhausted && retryPolicy.throwing()) {
            throw new RetriesExhaustedException(RETRIES_OR_EXECUTIONS_EXHAUSTED);
        }
        return exhausted;
    }

    private boolean timeExhausted() {
        return now().isAfter(startTime.plus(retryPolicy.timeout()));
    }

    private boolean executionsExhausted() {
        if (retryPolicy.maximumExecutions() <= 0) {
            return false;
        }
        return currentExecutions.sum() == retryPolicy.maximumExecutions();
    }

    private void sleep() {
        try {
            TimeUnit.NANOSECONDS.sleep(retryPolicy.interval().toNanos());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void setStartTime() {
        if (startTime == null) {
            startTime = now();
        }
    }
}