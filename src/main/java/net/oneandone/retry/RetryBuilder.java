package net.oneandone.retry;

import com.google.common.base.Throwables;
import net.oneandone.exception.RetriesExhaustedException;
import net.oneandone.exception.RetryException;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static java.lang.String.format;
import static java.time.LocalTime.now;
import static org.slf4j.LoggerFactory.getLogger;

public class RetryBuilder<T> {

    private static final String DURATION_FORMAT = "Duration: [%s]";
    private static final String TIMEOUT_FORMAT = "Timeout: [%s]";
    private static final String RETRIES_FORMAT = "Retries: [%s]";

    private Predicate<Exception> throwCondition = exception -> false;
    private Predicate<T> retryCondition = k -> false;

    private Duration timeout = Duration.of(30, ChronoUnit.SECONDS);
    private Duration interval = Duration.ZERO;

    private long retries = -1;
    private boolean silent = false;

    public Retry<T> build() {
        return new RetryImpl<>(this);
    }

    /**
     * Don't throw {@link RetriesExhaustedException}
     *
     * @return self
     */
    public RetryBuilder<T> silently() {
        this.silent = true;
        return this;
    }

    public RetryBuilder<T> withTimeout(long duration, ChronoUnit unit) {
        checkArgument(duration >= 0, format(DURATION_FORMAT, interval));
        checkNotNull(unit, "unit");
        return withTimeout(Duration.of(duration, unit));
    }

    public RetryBuilder<T> withTimeout(Duration timeout) {
        checkNotNull(timeout, "timeout");
        checkArgument(timeout.getNano() >= 0, format(TIMEOUT_FORMAT, timeout));
        this.timeout = timeout;
        return this;
    }

    public RetryBuilder<T> withInterval(Duration interval) {
        checkNotNull(interval, "timeout");
        checkArgument(interval.getNano() >= 0, format(DURATION_FORMAT, interval));
        this.interval = interval;
        return this;
    }

    public RetryBuilder<T> withInterval(long duration, ChronoUnit unit) {
        checkArgument(duration >= 0, format(DURATION_FORMAT, interval));
        checkNotNull(unit, "unit");
        return withInterval(Duration.of(duration, unit));
    }

    public RetryBuilder<T> withRetries(int retries) {
        checkArgument(retries >= 0, format(RETRIES_FORMAT, retries));
        this.retries = retries;
        return this;
    }

    public RetryBuilder<T> retryOnException(Predicate<Exception> throwCondition) {
        checkNotNull(throwCondition, "throwCondition");
        this.throwCondition = throwCondition;
        return this;
    }

    public RetryBuilder<T> retryUntil(Predicate<T> stopCondition) {
        checkNotNull(stopCondition, "stopCondition");
        return retryOn(stopCondition.negate());
    }

    public RetryBuilder<T> retryOn(Predicate<T> retryCondition) {
        checkNotNull(retryCondition, "retryCondition");
        this.retryCondition = retryCondition;
        return this;
    }

    private static final class RetryImpl<K> implements Retry<K> {

        private static final Logger LOG = getLogger(RetryImpl.class);

        private final Duration interval;
        private final Duration timeout;

        private final Predicate<Exception> throwCondition;
        private final Predicate<K> retryCondition;

        private final long retries;
        private final boolean silent;
        private long left;

        private LocalTime startTime;

        public Duration getInterval() {
            return interval;
        }

        public Duration getTimeout() {
            return timeout;
        }

        public Predicate<Exception> getThrowCondition() {
            return throwCondition;
        }

        public Predicate<K> getRetryCondition() {
            return retryCondition;
        }

        public long getRetries() {
            return retries;
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

        private RetryImpl(RetryBuilder<K> retryBuilder) {
            this.timeout = retryBuilder.timeout != null ? retryBuilder.timeout : Duration.of(30, ChronoUnit.SECONDS);
            this.retries = retryBuilder.retries;
            this.interval = retryBuilder.interval;
            this.left = retries;
            this.throwCondition = retryBuilder.throwCondition;
            this.retryCondition = retryBuilder.retryCondition;
            this.silent = retryBuilder.silent;
        }

        @Override
        public K call(Callable<K> task) {
            final var m = "No retries left or time is up";
            checkStartTime();
            try {
                final var taskResult = task.call();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Task result {type: {}, value: {}}", taskResult.getClass(), taskResult);
                    LOG.debug("Exhausted: {}", isExhausted());
                }
                if (isExhausted()) {
                    LOG.info(m);
                    if (silent) {
                        return taskResult;
                    } else {
                        throw new RetriesExhaustedException(m);
                    }
                }
                final var isRetryConditionSatisfied = retryCondition.test(taskResult);
                LOG.debug("Retry condition satisfied: {}", isRetryConditionSatisfied);
                if (isRetryConditionSatisfied) {
                    return retry(task);
                }
                return taskResult;
            } catch (Exception exception) {
                Throwables.throwIfInstanceOf(exception, RetriesExhaustedException.class);
                final var isThrowConditionSatisfied = throwCondition.test(exception);
                LOG.debug("Throw condition satisfied: {}", isThrowConditionSatisfied);
                if (isExhausted()) {
                    LOG.info(m, exception);
                    if (!silent) {
                        throw new RetriesExhaustedException(m, exception);
                    }
                }
                if (isThrowConditionSatisfied) {
                    return retry(task);
                }
                throw new RetryException(exception);
            }
        }

        private void checkStartTime() {
            if (startTime == null) {
                startTime = now();
                if (LOG.isInfoEnabled()) {
                    LOG.info(toString());
                }
            }
        }

        private K retry(Callable<K> task) {
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

        @Override
        public String toString() {
            return "Retry {" +
                    "interval: " + interval +
                    ", timeout: " + timeout +
                    ", throwCondition: " + throwCondition +
                    ", retryCondition: " + retryCondition +
                    ", silent: " + silent +
                    ", retries: " + retries +
                    ", left: " + left +
                    ", startTime: " + startTime +
                    '}';
        }
    }
}
