/*
 *  Copyright 2021 Gabriel Diegel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.github.gdiegel.retry.policy;

import io.github.gdiegel.retry.exception.RetriesExhaustedException;

import java.time.Duration;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * Allows fluently building a {@link RetryPolicy} of {@link RESULT} using sensible defaults.
 *
 * @param <RESULT> the type of the result of the computation
 * @author Gabriel Diegel
 */
public class RetryPolicyBuilder<RESULT> {

    private static final String INTERVAL_FORMAT = "Interval: [%s]";
    private static final String TIMEOUT_FORMAT = "Timeout: [%s]";
    private static final String MAXIMUM_EXECUTIONS_FORMAT = "Maximum executions: [%s]";

    /**
     * Default: Interval of 10 milliseconds between executions
     */
    private Duration interval = Duration.ofMillis(10);

    /**
     * Default: 30 second timeout
     */
    private Duration timeout = Duration.ofSeconds(30);

    /**
     * Default: No upper bound, will retry run until a stop condition occurs, a non-ignorable exception is thrown or the
     * given timeout is reached
     */
    private long maximumExecutions = -1;

    /**
     * Default: False, i.e. no exception will be ignored, in other words, any exception will break the execution chain.
     */
    private Predicate<Exception> ignorableException = exception -> false;

    /**
     * Default: False, will retry until exhausted, i.e. the given timeout is reached or the given maximum number of
     * executions have been performed.
     */
    private Predicate<RESULT> stopCondition = result -> false;

    /**
     * Default: Don't throw {@link RetriesExhaustedException}
     */
    private boolean throwing = false;

    /**
     * Return a fluent {@link RetryPolicyBuilder} of {@link RESULT}.
     *
     * @param <RESULT> the type of the result of the computation
     * @return an instance of {@link RetryPolicyBuilder} of {@link RESULT}
     */
    public static <RESULT> RetryPolicyBuilder<RESULT> instance() {
        return new RetryPolicyBuilder<>();
    }

    /**
     * @param interval a {@link Duration} representing the interval between executions
     * @return self
     */
    public RetryPolicyBuilder<RESULT> withInterval(Duration interval) {
        checkNotNull(interval, "timeout");
        checkArgument(interval.getNano() >= 0, format(INTERVAL_FORMAT, interval));
        this.interval = interval;
        return this;
    }

    /**
     * @param timeout a {@link Duration} representing the absolute timeout after which executions will considered to be
     * exhausted and aborted
     * @return self
     */
    public RetryPolicyBuilder<RESULT> withTimeout(Duration timeout) {
        checkNotNull(timeout, "timeout");
        checkArgument(timeout.getNano() >= 0, format(TIMEOUT_FORMAT, timeout));
        this.timeout = timeout;
        return this;
    }

    /**
     * @param maximumExecutions a long representing the absolute number of executions after which executions will
     * considered to be exhausted and aborted.
     * @return self
     */
    public RetryPolicyBuilder<RESULT> withMaximumExecutions(long maximumExecutions) {
        checkArgument(maximumExecutions >= 0, format(MAXIMUM_EXECUTIONS_FORMAT, maximumExecutions));
        this.maximumExecutions = maximumExecutions;
        return this;
    }

    /**
     * Add predicate which will evaluated on any {@link Exception} thrown during computation. If the predicate matches
     * the exception, the exception will be ignored and the computation will be continued. If the predicate doesn't
     * match the exception, the exception will be re-thrown.
     *
     * @param ignorableException a {@link Predicate} of {@link Exception} representing exceptions to ignore during
     * computation
     * @return self
     */
    public RetryPolicyBuilder<RESULT> ignoreWhen(Predicate<Exception> ignorableException) {
        checkNotNull(ignorableException, "ignorableException");
        this.ignorableException = ignorableException;
        return this;
    }

    /**
     * Add predicate which will evaluated on {@link RESULT} after every execution. If the predicate matches the result,
     * the computation will be stopped and the result returned. If the predicate doesn't match the result, the
     * computation will be continued.
     *
     * @param stopCondition a {@link Predicate} of {@link RESULT} representing a successful computation, after which
     * executions should be stopped
     * @return self
     */
    public RetryPolicyBuilder<RESULT> retryUntil(Predicate<RESULT> stopCondition) {
        checkNotNull(stopCondition, "stopCondition");
        this.stopCondition = stopCondition;
        return this;
    }

    /**
     * @param throwing Throw {@link RetriesExhaustedException} instead of returning the result when retries are
     * exhausted.
     * @return self
     */
    public RetryPolicyBuilder<RESULT> throwing(boolean throwing) {
        this.throwing = throwing;
        return this;
    }

    public RetryPolicy<RESULT> build() {
        return new RetryPolicy<>(this.interval, this.timeout, this.maximumExecutions, this.ignorableException, this.stopCondition, this.throwing);
    }

}
