package io.github.gdiegel.retry;

import io.github.gdiegel.exception.RetriesExhaustedException;
import org.jetbrains.annotations.Contract;

import java.time.Duration;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

public class RetryBuilder<RESULT> {

    private static final String DURATION_FORMAT = "Duration: [%s]";
    private static final String TIMEOUT_FORMAT = "Timeout: [%s]";
    private static final String RETRIES_FORMAT = "Retries: [%s]";

    /**
     * Defaults to false, i.e. no exception will be ignored. In other words, every exception will break the retry chain.
     */
    private Predicate<Exception> ignorableException = exception -> false;
    /**
     * Defaults to false, retries will be performed until exhausted, i.e. the given timeout is reached
     * or the given maximum number of retries have been performed.
     */
    private Predicate<RESULT> stopCondition = result -> false;

    private Duration timeout = Duration.ofSeconds(10);
    private Duration interval = Duration.ofMillis(10);

    private long maxExecutions = -1;
    private boolean throwing = false;

    public Retry<RESULT> build() {
        return new DefaultRetry<>(this.interval, this.timeout, this.ignorableException, this.stopCondition, this.maxExecutions, this.throwing);
    }

    /**
     * Throw {@link RetriesExhaustedException} instead fo returning the result when retries are exhausted.
     *
     * @return self
     */
    @Contract(" -> this")
    public RetryBuilder<RESULT> throwing() {
        this.throwing = true;
        return this;
    }

    @Contract("_ -> this")
    public RetryBuilder<RESULT> withTimeout(Duration timeout) {
        checkNotNull(timeout, "timeout");
        checkArgument(timeout.getNano() >= 0, format(TIMEOUT_FORMAT, timeout));
        this.timeout = timeout;
        return this;
    }

    @Contract("_ -> this")
    public RetryBuilder<RESULT> withInterval(Duration interval) {
        checkNotNull(interval, "timeout");
        checkArgument(interval.getNano() >= 0, format(DURATION_FORMAT, interval));
        this.interval = interval;
        return this;
    }

    @Contract("_ -> this")
    public RetryBuilder<RESULT> withMaxExecutions(long maxExecutions) {
        checkArgument(maxExecutions >= 0, format(RETRIES_FORMAT, maxExecutions));
        this.maxExecutions = maxExecutions;
        return this;
    }

    @Contract("_ -> this")
    public RetryBuilder<RESULT> retryWhenException(Predicate<Exception> ignorableException) {
        checkNotNull(ignorableException, "ignorableException");
        this.ignorableException = ignorableException;
        return this;
    }

    @Contract("_ -> this")
    public RetryBuilder<RESULT> retryUntil(Predicate<RESULT> stopCondition) {
        checkNotNull(stopCondition, "stopCondition");
        this.stopCondition = stopCondition;
        return this;
    }
}
