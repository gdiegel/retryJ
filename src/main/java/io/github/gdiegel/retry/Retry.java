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

import io.github.gdiegel.retry.policy.RetryPolicy;

import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * {@link Retry} allows executing a {@link Callable} of {@code RESULT} zero or more times and will return an {@link
 * Optional} of {@code RESULT} holding an instance of type {@link RESULT} of the result of the computation or not.
 * Thanks to Ray Holder, Jean-Baptiste Nizet and Jonathan Halterman for the inspiration.
 *
 * @param <RESULT> the type of the result of the computation
 * @author Gabriel Diegel
 */
public interface Retry<RESULT> {

    /**
     * Set a {@link RetryPolicy} of {@code RESULT} for the computation.
     *
     * @param retryPolicy the retry policy to use when executing the computation
     * @param <RESULT> the type of the result of the computation
     * @return self
     */
    static <RESULT> Retry<RESULT> with(RetryPolicy<RESULT> retryPolicy) {
        return new DefaultRetry<>(retryPolicy);
    }

    /**
     * Execute the computation wrapped in {@link Callable}.
     *
     * @param retryableTask the retryable task
     * @return An {@link Optional} holding the result of the computation or not.
     */
    Optional<RESULT> call(Callable<RESULT> retryableTask);
}