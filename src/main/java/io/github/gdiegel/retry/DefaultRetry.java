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

import com.google.common.base.Preconditions;
import io.github.gdiegel.retry.policy.RetryPolicy;

import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.Callable;

/**
 * Default implementation of {@link Retry}. An instance of {@link DefaultRetry} allows executing a {@link Callable} as
 * many times as necessary until either a timeout occurs, the configured number of executions is exhausted, a not
 * previously allowed exception is thrown or a stop condition is satisfied.
 *
 * @param <RESULT> the type of the result of the computation
 * @author Gabriel Diegel
 */
public final class DefaultRetry<RESULT> implements Retry<RESULT> {

    private final RetryPolicy<RESULT> retryPolicy;
    private final RetryExecutor<RESULT> retryExecutor;

    DefaultRetry(RetryPolicy<RESULT> retryPolicy) {
        Preconditions.checkNotNull(retryPolicy);
        this.retryPolicy = retryPolicy;
        this.retryExecutor = new RetryExecutor<>(retryPolicy);
    }

    @Override
    public Optional<RESULT> call(Callable<RESULT> callable) {
        Preconditions.checkNotNull(callable);
        return retryExecutor.execute(callable);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DefaultRetry.class.getSimpleName() + "[", "]")
                .add("retryPolicy=" + retryPolicy)
                .toString();
    }
}
