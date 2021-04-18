package io.github.gdiegel.retry;

import io.github.gdiegel.exception.RetriesExhaustedException;
import org.jetbrains.annotations.Contract;

import java.time.Duration;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

public class RetryPolicyBuilder<RESULT> {

    private static final String INTERVAL_FORMAT = "Interval: [%s]";
    private static final String TIMEOUT_FORMAT = "Timeout: [%s]";
    private static final String MAXIMUM_EXECUTIONS_FORMAT = "Maximum executions: [%s]";

    /**
     * Default: False, i.e. no exception will be ignored, in other words, any exception will break the execution chain.
     */
    private Predicate<Exception> ignorableException = exception -> false;
    /**
     * Default: False, will retry until exhausted, i.e. the given timeout is reached or the given maximum
     * number of executions have been performed.
     */
    private Predicate<RESULT> stopCondition = result -> false;

    /**
     * Default: 10 second timeout
     */
    private Duration timeout = Duration.ofSeconds(10);

    /**
     * Default: Interval of 10 milliseconds between executions
     */
    private Duration interval = Duration.ofMillis(10);

    /**
     * Default: No upper bound, will retry run until a stop condition occurs, a non-ignorable exception
     * is thrown or the given timeout is reached
     */
    private long maximumExecutions = -1;

    /**
     * Default: Don't throw {@link RetriesExhaustedException}
     */
    private boolean throwing = false;

    public static <RESULT> RetryPolicyBuilder<RESULT> instance(){
        return new RetryPolicyBuilder<>();
    }

    public RetryPolicy<RESULT> build() {
        return new RetryPolicy<>(this.interval, this.timeout, this.ignorableException, this.stopCondition, this.maximumExecutions, this.throwing);
    }

    /**
     * Throw {@link RetriesExhaustedException} instead of returning the result when retries are exhausted.
     *
     * @return self
     */
    @Contract(" -> this")
    public RetryPolicyBuilder<RESULT> throwing() {
        this.throwing = true;
        return this;
    }

    @Contract("_ -> this")
    public RetryPolicyBuilder<RESULT> withTimeout(Duration timeout) {
        checkNotNull(timeout, "timeout");
        checkArgument(timeout.getNano() >= 0, format(TIMEOUT_FORMAT, timeout));
        this.timeout = timeout;
        return this;
    }

    @Contract("_ -> this")
    public RetryPolicyBuilder<RESULT> withInterval(Duration interval) {
        checkNotNull(interval, "timeout");
        checkArgument(interval.getNano() >= 0, format(INTERVAL_FORMAT, interval));
        this.interval = interval;
        return this;
    }

    @Contract("_ -> this")
    public RetryPolicyBuilder<RESULT> withMaxExecutions(long maxExecutions) {
        checkArgument(maxExecutions >= 0, format(MAXIMUM_EXECUTIONS_FORMAT, maxExecutions));
        this.maximumExecutions = maxExecutions;
        return this;
    }

    @Contract("_ -> this")
    public RetryPolicyBuilder<RESULT> retryWhenException(Predicate<Exception> ignorableException) {
        checkNotNull(ignorableException, "ignorableException");
        this.ignorableException = ignorableException;
        return this;
    }

    @Contract("_ -> this")
    public RetryPolicyBuilder<RESULT> retryUntil(Predicate<RESULT> stopCondition) {
        checkNotNull(stopCondition, "stopCondition");
        this.stopCondition = stopCondition;
        return this;
    }
}
