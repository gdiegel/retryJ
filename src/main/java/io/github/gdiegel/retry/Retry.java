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
import io.github.gdiegel.retry.executor.DefaultRetryExecutor;
import io.github.gdiegel.retry.executor.RetryExecutor;
import io.github.gdiegel.retry.policy.RetryPolicy;

import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * {@link Retry} allows executing a {@link Callable} of {@code RESULT} zero or more times and will return an {@link
 * Optional} of {@code RESULT} holding an instance of type {@code RESULT} of the result of the computation.
 * Thanks to Ray Holder, Jean-Baptiste Nizet and Jonathan Halterman for the inspiration.
 *
 * @author Gabriel Diegel
 */
public interface Retry {

    /**
     * Set a {@link RetryPolicy} of {@code RESULT} for the computation.
     *
     * @param retryPolicy the retry policy to use when executing the computation
     * @param <RESULT> the type of the result of the computation
     * @return An instance of {@link RetryExecutor} of {@code RESULT}
     */
    static <RESULT> RetryExecutor<RESULT> with(RetryPolicy<RESULT> retryPolicy) {
        Preconditions.checkNotNull(retryPolicy);
        return new DefaultRetryExecutor<>(retryPolicy);
    }
}
