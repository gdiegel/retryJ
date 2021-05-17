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

import java.time.Duration;
import java.util.function.Predicate;

/**
 * A {@link RetryPolicy} of {@code RESULT} allows configuring exactly how often the computation should be executed and
 * under which conditions it should be aborted.
 *
 * @param <RESULT> the type of the result of the computation
 * @author Gabriel Diegel
 */
public record RetryPolicy<RESULT>(Duration interval, Duration timeout, long maximumExecutions,
                                  Predicate<Exception> ignorableException,
                                  Predicate<RESULT> stopCondition, boolean throwing) {

    /**
     * Return a fluent {@link RetryPolicyBuilder} of {@code RESULT}.
     *
     * @param <RESULT> the type of the result of the computation
     * @return an instance of {@link RetryPolicyBuilder} of {@code RESULT}
     */
    public static <RESULT> RetryPolicyBuilder<RESULT> builder() {
        return RetryPolicyBuilder.instance();
    }
}
