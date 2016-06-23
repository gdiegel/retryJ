package net.oneandone.retry;

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
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.slf4j.LoggerFactory.getLogger;

public class RetrierBuilder<T> {

    private static final Duration DEFAULT_TIMEOUT = Duration.of(1, MINUTES);
    private static final Duration DEFAULT_INTERVAL = Duration.of(100, MILLIS);
    private static final long DEFAULT_RETRY_TIMES = 600;

    private Predicate<Exception> throwCondition = exception -> false;
    private Predicate<? extends T> retryCondition = k -> false;
    private Duration timeout;
    private Duration interval = DEFAULT_INTERVAL;
    private long times;

    public Retrier<T> build() {
        return new RetrierImpl<>(this);
    }

    public RetrierBuilder<T> withTimeout(long duration, ChronoUnit chronoUnit) {
        checkArgument(duration >= 0, "duration '" + duration + "'");
        checkNotNull(chronoUnit, "chronoUnit");
        return withTimeout(Duration.of(duration, chronoUnit));
    }

    public RetrierBuilder<T> withTimeout(Duration timeout) {
        checkNotNull(timeout, "timeout");
        this.timeout = timeout;
        return this;
    }

    public RetrierBuilder<T> withInterval(Duration duration) {
        checkArgument(duration.getNano() >= 0, "duration '" + duration + "'");
        this.interval = duration;
        return this;
    }

    public RetrierBuilder<T> withInterval(long duration, ChronoUnit chronoUnit) {
        checkArgument(duration >= 0, "duration '" + duration + "'");
        checkNotNull(chronoUnit, "chronoUnit");
        return withInterval(Duration.of(duration, chronoUnit));
    }

    public RetrierBuilder<T> withRetries(int times) {
        checkArgument(times >= 0, "times '" + times + "'");
        this.times = times;
        return this;
    }

    public RetrierBuilder<T> retryOnException(Predicate<Exception> throwCondition) {
        checkNotNull(throwCondition, "throwCondition");
        this.throwCondition = throwCondition;
        return this;
    }

    public RetrierBuilder<T> retryUntil(Predicate<? extends T> stopCondition) {
        checkNotNull(stopCondition, "stopCondition");
        return retryOn(stopCondition.negate());
    }

    public RetrierBuilder<T> retryOn(Predicate<? extends T> retryCondition) {
        checkNotNull(retryCondition, "retryCondition");
        this.retryCondition = retryCondition;
        return this;
    }

    private static final class RetrierImpl<K> implements Retrier<K> {

        private static final Logger LOG = getLogger(RetrierImpl.class);

        private final Duration interval;
        private final Duration timeout;
        private final Predicate<Exception> throwCondition;
        private final Predicate<K> retryCondition;
        private final long times;
        private long left;
        private LocalTime startTime;

        private RetrierImpl(RetrierBuilder retrierBuilder) {
            this.timeout = retrierBuilder.timeout != null ? retrierBuilder.timeout : DEFAULT_TIMEOUT;
            this.times = retrierBuilder.times != 0 ? retrierBuilder.times : DEFAULT_RETRY_TIMES;
            this.interval = retrierBuilder.interval;
            this.left = times;
            this.throwCondition = retrierBuilder.throwCondition;
            this.retryCondition = retrierBuilder.retryCondition;
            LOG.info(this.toString());
        }

        @Override
        public final K call(Callable<K> task) throws RetryException {
            if (startTime == null) {
                startTime = now();
                LOG.debug("Start time: " + startTime.toString());
            }
            final boolean exhausted = isExhausted();
            LOG.debug("Exhausted: {}", exhausted);
            try {
                final K taskResult = task.call();
                LOG.debug("Returning task result of type: {}", taskResult.getClass());
                final boolean isRetryConditionSatisfied = retryCondition.test(taskResult);
                LOG.debug(taskResult.toString());
                LOG.debug("Retry condition satisfied: {}", isRetryConditionSatisfied);
                if (!exhausted && isRetryConditionSatisfied) {
                    return retry(task);
                }
                return taskResult;
            } catch (Exception exception) {
                final boolean isThrowConditionSatisfied = throwCondition.test(exception);
                LOG.debug("Throw condition satisfied: {}", isThrowConditionSatisfied);
                if (!exhausted && isThrowConditionSatisfied) {
                    return retry(task);
                }
                throw new RetriesExhaustedException("Exhausted all retries", exception);
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
            return "Retrier{" +
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



