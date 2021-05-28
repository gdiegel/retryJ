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

import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * A {@link RetryExecutor} of {@code RESULT} executes the {@link Callable} passed to it and returns an {@link Optional}
 * of {@code RESULT}.
 *
 * @param <RESULT> the type of the result of the computation
 * @author Gabriel Diegel
 */
public interface RetryExecutor<RESULT> {

    /**
     * Execute the computation by calling the {@link Callable}.
     *
     * @param callable The callable to execute
     * @return An {@link Optional} of {@code RESULT} holding the result of the computation
     */
    Optional<RESULT> execute(Callable<RESULT> callable);
}
