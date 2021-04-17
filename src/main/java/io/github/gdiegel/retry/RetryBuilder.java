package io.github.gdiegel.retry;

import io.github.gdiegel.exception.RetriesExhaustedException;
import org.jetbrains.annotations.Contract;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

public class RetryBuilder<T> {

    private static final String DURATION_FORMAT = "Duration: [%s]";
    private static final String TIMEOUT_FORMAT = "Timeout: [%s]";
    private static final String RETRIES_FORMAT = "Retries: [%s]";

    /**
     * Defaults to false, i.e. no exception will be ignored. In other words, every exception will break the retry chain.
     */
    private Predicate<Exception> ignorableException = (Exception exception) -> false;
    /**
     * Defaults to false, retries will be performed until exhausted, i.e. the given timeout is reached
     * or the given maximum number of retries have been performed.
     */
    private Predicate<T> stopCondition = (T t) -> false;

    private Duration timeout = Duration.of(30, ChronoUnit.SECONDS);
    private Duration interval = Duration.ZERO;

    private long maxRetries = -1;
    private boolean silent = false;

    public Retry<T> build() {
        return new DefaultRetry<>(this.interval, this.timeout, this.ignorableException, this.stopCondition, this.maxRetries, this.silent);
    }

    /**
     * Don't throw {@link RetriesExhaustedException}
     *
     * @return self
     */
    @Contract(" -> this")
    public RetryBuilder<T> silently() {
        this.silent = true;
        return this;
    }

    @Contract("_ -> this")
    public RetryBuilder<T> withTimeout(Duration timeout) {
        checkNotNull(timeout, "timeout");
        checkArgument(timeout.getNano() >= 0, format(TIMEOUT_FORMAT, timeout));
        this.timeout = timeout;
        return this;
    }

    @Contract("_ -> this")
    public RetryBuilder<T> withInterval(Duration interval) {
        checkNotNull(interval, "timeout");
        checkArgument(interval.getNano() >= 0, format(DURATION_FORMAT, interval));
        this.interval = interval;
        return this;
    }

    @Contract("_ -> this")
    public RetryBuilder<T> withMaxRetries(int maxRetries) {
        checkArgument(maxRetries >= 0, format(RETRIES_FORMAT, maxRetries));
        this.maxRetries = maxRetries;
        return this;
    }

    @Contract("_ -> this")
    public RetryBuilder<T> retryWhenException(Predicate<Exception> ignorableException) {
        checkNotNull(ignorableException, "ignorableException");
        this.ignorableException = ignorableException;
        return this;
    }

    @Contract("_ -> this")
    public RetryBuilder<T> retryUntil(Predicate<T> stopCondition) {
        checkNotNull(stopCondition, "stopCondition");
        this.stopCondition = stopCondition;
        return this;
    }
}
