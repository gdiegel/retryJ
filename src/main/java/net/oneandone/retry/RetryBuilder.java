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
import static java.time.LocalTime.now;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.slf4j.LoggerFactory.getLogger;

public class RetryBuilder<T> {

    private static final Duration DEFAULT_TIMEOUT = Duration.of(30, SECONDS);
    private static final Duration DEFAULT_INTERVAL = Duration.of(100, MILLIS);
    private static final long DEFAULT_RETRY_TIMES = 300;

    private Predicate<Exception> throwCondition = exception -> false;
    private Predicate<T> retryCondition = k -> false;
    private Duration timeout;
    private Duration interval = DEFAULT_INTERVAL;
    private long times;

    public Retry<T> build() {
        return new RetryImpl<>(this);
    }

    public RetryBuilder<T> withTimeout(long duration, ChronoUnit unit) {
        checkArgument(duration >= 0, "Duration: [" + duration + "]");
        checkNotNull(unit, "unit");
        return withTimeout(Duration.of(duration, unit));
    }

    public RetryBuilder<T> withTimeout(Duration timeout) {
        checkNotNull(timeout, "timeout");
        checkArgument(timeout.getNano() >= 0, "Timeout: [" + timeout + "]");
        this.timeout = timeout;
        return this;
    }

    public RetryBuilder<T> withInterval(Duration interval) {
        checkNotNull(interval, "timeout");
        checkArgument(interval.getNano() >= 0, "Duration: [" + interval + "]");
        this.interval = interval;
        return this;
    }

    public RetryBuilder<T> withInterval(long duration, ChronoUnit unit) {
        checkArgument(duration >= 0, "Duration: [" + duration + "]");
        checkNotNull(unit, "unit");
        return withInterval(Duration.of(duration, unit));
    }

    public RetryBuilder<T> withRetries(int times) {
        checkArgument(times >= 0, "Retries: [" + times + "]");
        this.times = times;
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
        private final long times;
        private long left;
        private LocalTime startTime;

        private RetryImpl(RetryBuilder<K> retryBuilder) {
            this.timeout = retryBuilder.timeout != null ? retryBuilder.timeout : DEFAULT_TIMEOUT;
            this.times = retryBuilder.times != 0 ? retryBuilder.times : DEFAULT_RETRY_TIMES;
            this.interval = retryBuilder.interval;
            this.left = times;
            this.throwCondition = retryBuilder.throwCondition;
            this.retryCondition = retryBuilder.retryCondition;
            LOG.info(this.toString());
        }

        @Override
        public K call(Callable<K> task) throws RetryException {
            String m = "No retries left or time is up";
            checkStartTime();
            try {
                final K taskResult = task.call();
                LOG.debug("Task result is of type: {}", taskResult.getClass());
                LOG.debug(taskResult.toString());
                LOG.debug("Exhausted: {}", isExhausted());
                if (isExhausted()) {
                    LOG.info(m);
                    throw new RetriesExhaustedException(m);
                }
                final boolean isRetryConditionSatisfied = retryCondition.test(taskResult);
                LOG.debug("Retry condition satisfied: {}", isRetryConditionSatisfied);
                if (isRetryConditionSatisfied) {
                    return retry(task);
                }
                return taskResult;
            } catch (Exception exception) {
                Throwables.propagateIfInstanceOf(exception, RetriesExhaustedException.class);
                final boolean isThrowConditionSatisfied = throwCondition.test(exception);
                LOG.debug("Throw condition satisfied: {}", isThrowConditionSatisfied);
                if (isExhausted()) {
                    LOG.info(m, exception);
                    throw new RetriesExhaustedException(m, exception);
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
                LOG.debug("Start time: " + startTime.toString());
            }
        }

        private K retry(Callable<K> task) throws RetryException {
            LOG.debug("Sleeping for {}", interval);
            if (interval.getSeconds() != 0) {
                sleepUninterruptibly(interval.getSeconds(), TimeUnit.SECONDS);
            } else {
                sleepUninterruptibly(interval.getNano(), TimeUnit.NANOSECONDS);
            }
            left--;
            LOG.debug("Attempts left: {}", left);
            return call(task);
        }

        private boolean isExhausted() {
            return left == 0 || now().isAfter(startTime.plus(timeout));
        }

        @Override
        public String toString() {
            return "Retry{" +
                    "interval=" + interval +
                    ", timeout=" + timeout +
                    ", throwCondition=" + throwCondition +
                    ", retryCondition=" + retryCondition +
                    ", times=" + times +
                    ", left=" + left +
                    ", startTime=" + startTime +
                    '}';
        }
    }
}


