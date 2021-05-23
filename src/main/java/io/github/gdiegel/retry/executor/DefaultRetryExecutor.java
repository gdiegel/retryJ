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
package io.github.gdiegel.retry.executor;

import com.google.common.annotations.VisibleForTesting;
import io.github.gdiegel.retry.exception.RetriesExhaustedException;
import io.github.gdiegel.retry.exception.RetryException;
import io.github.gdiegel.retry.policy.RetryPolicy;

import java.time.LocalTime;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import static java.time.LocalTime.now;

/**
 * Default implementation of {@link RetryExecutor} of {@link RESULT}. Follows a {@link RetryPolicy} when executing the
 * computation.
 *
 * @param <RESULT> the type of the result of the computation
 * @author Gabriel Diegel
 */
public class DefaultRetryExecutor<RESULT> implements RetryExecutor<RESULT> {

    private static final String RETRIES_OR_EXECUTIONS_EXHAUSTED = "Retries or executions exhausted";
    private final RetryPolicy<RESULT> retryPolicy;

    private LocalTime startTime;
    private final LongAdder currentExecutions = new LongAdder();

    public DefaultRetryExecutor(RetryPolicy<RESULT> retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    @VisibleForTesting
    long getCurrentExecutions() {
        return currentExecutions.sum();
    }

    @Override
    public Optional<RESULT> execute(Callable<RESULT> callable) {
        if (retryPolicy.maximumExecutions() == 0) {
            return Optional.empty();
        }
        return doExecute(callable);
    }

    private Optional<RESULT> doExecute(Callable<RESULT> callable) {
        Optional<RESULT> result = Optional.empty();
        setStartTime();
        do {
            try {
                currentExecutions.increment();
                result = Optional.ofNullable(callable.call());
            } catch (Exception e) {
                if (!retryPolicy.ignorableException().test(e)) {
                    throw new RetryException(e);
                }
            }
            sleep();
            if (result.isPresent() && retryPolicy.stopCondition().test(result.get())) {
                break;
            }
        } while (!exhausted());
        return result;
    }

    private boolean exhausted() {
        final boolean exhausted = timeExhausted() || executionsExhausted();
        if (exhausted && retryPolicy.throwing()) {
            throw new RetriesExhaustedException(RETRIES_OR_EXECUTIONS_EXHAUSTED);
        }
        return exhausted;
    }

    private boolean timeExhausted() {
        return now().isAfter(startTime.plus(retryPolicy.timeout()));
    }

    private boolean executionsExhausted() {
        if (retryPolicy.maximumExecutions() <= 0) {
            return false;
        }
        return currentExecutions.sum() == retryPolicy.maximumExecutions();
    }

    private void sleep() {
        try {
            TimeUnit.NANOSECONDS.sleep(retryPolicy.interval().toNanos());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void setStartTime() {
        if (startTime == null) {
            startTime = now();
        }
    }
}