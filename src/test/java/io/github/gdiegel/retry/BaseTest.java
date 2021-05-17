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
package io.github.gdiegel.retry;

import io.github.gdiegel.retry.executor.RetryExecutor;
import io.github.gdiegel.retry.policy.RetryPolicy;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

public abstract class BaseTest {
    protected static final Callable<Integer> IDEMPOTENT_CALLABLE = () -> 1;
    protected static final Duration INTERVAL = Duration.ofSeconds(1);
    protected static final Duration TIMEOUT = Duration.ofDays(1);
    protected static final long MAXIMUM_EXECUTIONS = 1000L;
    protected static final Predicate<Exception> IGNORABLE_EXCEPTION = e -> e.getCause().getClass() == StackOverflowError.class;
    protected static final Predicate<Integer> STOP_CONDITION = integer -> integer > 5;
    protected static final boolean THROWING = true;

    protected static final RetryPolicy<Integer> RETRY_POLICY = RetryPolicy.<Integer>builder().withInterval(INTERVAL)
            .withTimeout(TIMEOUT)
            .withMaximumExecutions(MAXIMUM_EXECUTIONS)
            .ignoreWhen(IGNORABLE_EXCEPTION)
            .retryUntil(STOP_CONDITION)
            .throwing(true)
            .build();

    protected static final RetryExecutor<Integer> RETRY = Retry.using(RETRY_POLICY);
}
