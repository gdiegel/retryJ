package io.github.gdiegel.retry;

import java.time.Duration;
import java.util.StringJoiner;
import java.util.function.Predicate;

/**
 * A {@link RetryPolicy<RESULT>} allows configuring exactly how often the computation should be executed and
 * under which conditions it should be aborted.
 *
 * @param <RESULT> the type of the result of the computation
 * @author Gabriel Diegel
 */
public class RetryPolicy<RESULT> {

    private final Duration interval;
    private final Duration timeout;

    private final Predicate<Exception> ignorableException;
    private final Predicate<RESULT> stopCondition;

    private final long maximumExecutions;
    private final boolean throwing;

    public static <RESULT> RetryPolicyBuilder<RESULT> builder() {
        return new RetryPolicyBuilder<>();
    }

    public RetryPolicy(Duration interval, Duration timeout, Predicate<Exception> ignorableException, Predicate<RESULT> stopCondition, long maximumExecutions, boolean throwing) {
        this.interval = interval;
        this.timeout = timeout;
        this.ignorableException = ignorableException;
        this.stopCondition = stopCondition;
        this.maximumExecutions = maximumExecutions;
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

    public long getMaximumExecutions() {
        return maximumExecutions;
    }

    public boolean isThrowing() {
        return throwing;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RetryPolicy.class.getSimpleName() + "[", "]")
                .add("interval=" + interval)
                .add("timeout=" + timeout)
                .add("ignorableException=" + ignorableException)
                .add("stopCondition=" + stopCondition)
                .add("maximumExecutions=" + maximumExecutions)
                .add("throwing=" + throwing)
                .toString();
    }
}
