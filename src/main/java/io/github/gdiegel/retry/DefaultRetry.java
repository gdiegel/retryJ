package io.github.gdiegel.retry;

import com.google.common.base.Throwables;
import io.github.gdiegel.exception.RetriesExhaustedException;
import io.github.gdiegel.exception.RetryException;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static java.time.LocalTime.now;
import static org.slf4j.LoggerFactory.getLogger;

public final class DefaultRetry<RESULT> implements Retry<RESULT> {

    private static final Logger LOG = getLogger(DefaultRetry.class);
    private static final String NO_RETRIES_LEFT_OR_TIME_IS_UP = "No retries left or time is up";

    private final Duration interval;
    private final Duration timeout;

    private final Predicate<Exception> ignorableException;
    private final Predicate<RESULT> stopCondition;

    private final long maxRetries;
    private final boolean silent;

    private long left;
    private LocalTime startTime;

    public DefaultRetry(Duration interval, Duration timeout, Predicate<Exception> ignorableException, Predicate<RESULT> stopCondition, long maxRetries, boolean silent) {
        this.interval = interval;
        this.timeout = timeout;
        this.ignorableException = ignorableException;
        this.stopCondition = stopCondition;
        this.maxRetries = maxRetries;
        this.silent = silent;
        this.left = maxRetries;
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

    public long getMaxRetries() {
        return maxRetries;
    }

    public boolean isSilent() {
        return silent;
    }

    public long getLeft() {
        return left;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    @Override
    public RESULT call(Callable<RESULT> task) {
        checkStartTime();
        try {
            final var taskResult = task.call();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Task result {type: {}, value: {}}", taskResult.getClass(), taskResult);
                LOG.debug("Exhausted: {}", isExhausted());
            }
            if (isExhausted()) {
                LOG.info(NO_RETRIES_LEFT_OR_TIME_IS_UP);
                if (silent) {
                    return taskResult;
                } else {
                    throw new RetriesExhaustedException(NO_RETRIES_LEFT_OR_TIME_IS_UP);
                }
            }
            final var isStopConditionSatisfied = stopCondition.test(taskResult);
            LOG.debug("Stop condition satisfied: {}", isStopConditionSatisfied);
            if (!isStopConditionSatisfied) {
                return retry(task);
            }
            return taskResult;
        } catch (Exception exception) {
            Throwables.throwIfInstanceOf(exception, RetriesExhaustedException.class);
            final var isThrowConditionSatisfied = ignorableException.test(exception);
            LOG.debug("Throw condition satisfied: {}", isThrowConditionSatisfied);
            if (isExhausted()) {
                LOG.info(NO_RETRIES_LEFT_OR_TIME_IS_UP, exception);
                if (!silent) {
                    throw new RetriesExhaustedException(NO_RETRIES_LEFT_OR_TIME_IS_UP, exception);
                }
            }
            if (isThrowConditionSatisfied) {
                return retry(task);
            }
            throw new RetryException(exception);
        }
    }

    private RESULT retry(Callable<RESULT> task) {
        LOG.debug("Sleeping for {}", interval);
        if (interval.getSeconds() != 0) {
            sleepUninterruptibly(interval.getSeconds(), TimeUnit.SECONDS);
        } else {
            sleepUninterruptibly(interval.getNano(), TimeUnit.NANOSECONDS);
        }
        if (left > 0) {
            left--;
        }
        LOG.debug("Attempts left: {}", left);
        return call(task);
    }

    private boolean isExhausted() {
        return left == 0 || now().isAfter(startTime.plus(timeout));
    }

    private void checkStartTime() {
        if (startTime == null) {
            startTime = now();
            if (LOG.isInfoEnabled()) {
                LOG.info(toString());
            }
        }
    }

    @Override
    public String toString() {
        return "Retry {" +
                "interval: " + interval +
                ", timeout: " + timeout +
                ", ignorableExcetion: " + ignorableException +
                ", stopCondition: " + stopCondition +
                ", silent: " + silent +
                ", maxRetries: " + maxRetries +
                ", left: " + left +
                ", startTime: " + startTime +
                '}';
    }
}
