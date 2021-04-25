package io.github.gdiegel.retry.policy;

import io.github.gdiegel.retry.exception.RetriesExhaustedException;

import java.time.Duration;
import java.util.StringJoiner;
import java.util.function.Predicate;

/**
 * A {@link RetryPolicy<RESULT>} allows configuring exactly how often the computation should be executed and under which
 * conditions it should be aborted.
 *
 * @param <RESULT> the type of the result of the computation
 * @author Gabriel Diegel
 */
public class RetryPolicy<RESULT> {

    private final Duration interval;
    private final Duration timeout;
    private final long maximumExecutions;
    private final Predicate<Exception> ignorableException;
    private final Predicate<RESULT> stopCondition;
    private final boolean throwing;

    RetryPolicy(Duration interval, Duration timeout, long maximumExecutions, Predicate<Exception> ignorableException, Predicate<RESULT> stopCondition, boolean throwing) {
        this.interval = interval;
        this.timeout = timeout;
        this.maximumExecutions = maximumExecutions;
        this.ignorableException = ignorableException;
        this.stopCondition = stopCondition;
        this.throwing = throwing;
    }

    /**
     * Return a fluent {@link RetryPolicyBuilder<RESULT>}.
     *
     * @param <RESULT> the type of the result of the computation
     * @return an instance of {@link RetryPolicyBuilder<RESULT>}
     */
    public static <RESULT> RetryPolicyBuilder<RESULT> builder() {
        return new RetryPolicyBuilder<>();
    }

    /**
     * @return a {@link Duration} representing the interval between executions
     */
    public Duration getInterval() {
        return interval;
    }

    /**
     * @return a {@link Duration} representing the absolute timeout after which executions will considered to be
     * exhausted and aborted
     */
    public Duration getTimeout() {
        return timeout;
    }

    /**
     * @return a long representing the absolute number of executions after which executions will considered to be
     * exhausted and aborted
     */
    public long getMaximumExecutions() {
        return maximumExecutions;
    }

    /**
     * @return a {@link Predicate<Exception>} representing exceptions to ignore during computation
     */
    public Predicate<Exception> getIgnorableException() {
        return ignorableException;
    }

    /**
     * @return a {@link Predicate<RESULT>} representing a successful computation, after which executions should be
     * stopped
     */
    public Predicate<RESULT> getStopCondition() {
        return stopCondition;
    }

    /**
     * @return a boolean indicating if {@link RetriesExhaustedException} should be thrown instead of returning the
     * result if retries are exhausted
     */
    public boolean isThrowing() {
        return throwing;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RetryPolicy.class.getSimpleName() + "[", "]")
                .add("interval=" + interval)
                .add("timeout=" + timeout)
                .add("maximumExecutions=" + maximumExecutions)
                .add("ignorableException=" + ignorableException)
                .add("stopCondition=" + stopCondition)
                .add("throwing=" + throwing)
                .toString();
    }
}
