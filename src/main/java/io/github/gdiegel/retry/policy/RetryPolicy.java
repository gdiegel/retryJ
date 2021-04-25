package io.github.gdiegel.retry.policy;

import io.github.gdiegel.retry.exception.RetriesExhaustedException;

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

    RetryPolicy(Duration interval, Duration timeout, Predicate<Exception> ignorableException, Predicate<RESULT> stopCondition, long maximumExecutions, boolean throwing) {
        this.interval = interval;
        this.timeout = timeout;
        this.ignorableException = ignorableException;
        this.stopCondition = stopCondition;
        this.maximumExecutions = maximumExecutions;
        this.throwing = throwing;
    }

    /**
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
     * @return a {@link Duration} representing the absolute timeout after which executions will considered to be exhausted and aborted
     */
    public Duration getTimeout() {
        return timeout;
    }

    /**
     * @return a {@link Predicate<Exception>} that matches exceptions to ignore during computation
     */
    public Predicate<Exception> getIgnorableException() {
        return ignorableException;
    }

    /**
     * @return a {@link Predicate<RESULT>} that matches a condition representing a successful computation, after which executions
     * should be stopped
     */
    public Predicate<RESULT> getStopCondition() {
        return stopCondition;
    }

    /**
     * @return a long representing the absolute number of executions
     * should be stopped
     */
    public long getMaximumExecutions() {
        return maximumExecutions;
    }

    /**
     * @return a boolean indicating if {@link RetriesExhaustedException} should be thrown if the
     * retries are exhausted
     */
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
