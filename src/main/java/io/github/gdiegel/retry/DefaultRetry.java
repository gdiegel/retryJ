package io.github.gdiegel.retry;

import io.github.gdiegel.exception.RetriesExhaustedException;
import io.github.gdiegel.exception.RetryException;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static java.time.LocalTime.now;
import static org.slf4j.LoggerFactory.getLogger;

public final class DefaultRetry<RESULT> implements Retry<RESULT> {

    private static final Logger LOG = getLogger(DefaultRetry.class);
    private static final String RETRIES_OR_EXECUTIONS_EXHAUSTED = "Retries or executions exhausted";

    private final Duration interval;
    private final Duration timeout;

    private final Predicate<Exception> ignorableException;
    private final Predicate<RESULT> stopCondition;

    private final long maxExecutions;
    private final boolean throwing;

    private final AtomicLong currentExecutions = new AtomicLong(0);
    private LocalTime startTime;

    public DefaultRetry(Duration interval, Duration timeout, Predicate<Exception> ignorableException, Predicate<RESULT> stopCondition, long maxExecutions, boolean throwing) {
        this.interval = interval;
        this.timeout = timeout;
        this.ignorableException = ignorableException;
        this.stopCondition = stopCondition;
        this.maxExecutions = maxExecutions;
        this.throwing = throwing;
    }

    public Duration getInterval() {
        return interval;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public Predicate<Exception> getIgnorableException() {
        return ignorableException;
    }

    public Predicate<RESULT> getStopCondition() {
        return stopCondition;
    }

    public long getMaxExecutions() {
        return maxExecutions;
    }

    public boolean isThrowing() {
        return throwing;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public Optional<RESULT> call(Callable<RESULT> callable) {
        Optional<RESULT> result = Optional.empty();
        if (maxExecutions == 0) {
            return result;
        }
        setStartTime();
        LOG.debug(this.toString());
        do {
            result = doCall(callable);
            sleepUninterruptibly(interval);
            if (result.isPresent() && stopCondition.test(result.get())) {
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
            if (!ignorableException.test(e)) {
                throw new RetryException(e);
            }
        }
        return call;
    }

    private boolean exhausted() {
        final boolean exhausted = executionsExhausted() || timeExhausted();
        if (exhausted && throwing) {
            throw new RetriesExhaustedException(RETRIES_OR_EXECUTIONS_EXHAUSTED);
        }
        return exhausted;
    }

    private boolean timeExhausted() {
        return now().isAfter(startTime.plus(timeout));
    }

    private boolean executionsExhausted() {
        if (maxExecutions <= 0) {
            return false;
        }
        return currentExecutions.get() == maxExecutions;
    }

    private void setStartTime() {
        if (startTime == null) {
            startTime = now();
        }
    }

    @Override
    public String toString() {
        return "Retry {" +
                "interval: " + interval +
                ", timeout: " + timeout +
                ", silent: " + throwing +
                ", maxExecutions: " + maxExecutions +
                ", currentExecutions: " + currentExecutions.get() +
                ", startTime: " + startTime +
                '}';
    }
}
