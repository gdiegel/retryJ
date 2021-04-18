package io.github.gdiegel.retry;

import io.github.gdiegel.exception.RetriesExhaustedException;
import io.github.gdiegel.exception.RetryException;
import org.slf4j.Logger;

import java.time.LocalTime;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static java.time.LocalTime.now;
import static org.slf4j.LoggerFactory.getLogger;

public final class DefaultRetry<RESULT> implements Retry<RESULT> {

    private static final Logger LOG = getLogger(DefaultRetry.class);
    private static final String RETRIES_OR_EXECUTIONS_EXHAUSTED = "Retries or executions exhausted";

    private final RetryPolicy<RESULT> retryPolicy;

    private final AtomicLong currentExecutions = new AtomicLong(0);
    private LocalTime startTime;

    DefaultRetry(RetryPolicy<RESULT> retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public Optional<RESULT> call(Callable<RESULT> callable) {
        Optional<RESULT> result = Optional.empty();
        if (retryPolicy.getMaximumExecutions() == 0) {
            return result;
        }
        setStartTime();
        LOG.debug(this.toString());
        do {
            result = doCall(callable);
            sleepUninterruptibly(retryPolicy.getInterval());
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

    @Override
    public String toString() {
        return new StringJoiner(", ", DefaultRetry.class.getSimpleName() + "[", "]")
                .add("retryPolicy=" + retryPolicy)
                .add("currentExecutions=" + currentExecutions.get())
                .add("startTime=" + startTime)
                .toString();
    }
}
