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

import io.github.gdiegel.retry.BaseTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RetryPolicyTest extends BaseTest {

    @Test
    void canCreateBuilderFromRetryPolicy() {
        final RetryPolicyBuilder<Integer> builder = RetryPolicy.builder();
        assertThat(builder).isNotNull();
    }

    @Test
    void canCreateInstanceOfRetryPolicyViaConstructor() {
        final RetryPolicy<Integer> retryPolicy = new RetryPolicy<>(INTERVAL, TIMEOUT, MAXIMUM_EXECUTIONS, IGNORABLE_EXCEPTION, STOP_CONDITION, THROWING);
        assertThat(retryPolicy).isNotNull();
        assertThat(retryPolicy.getInterval()).isEqualTo(INTERVAL);
        assertThat(retryPolicy.getTimeout()).isEqualTo(TIMEOUT);
        assertThat(retryPolicy.getMaximumExecutions()).isEqualTo(MAXIMUM_EXECUTIONS);
        assertThat(retryPolicy.getIgnorableException()).isEqualTo(IGNORABLE_EXCEPTION);
        assertThat(retryPolicy.getStopCondition()).isEqualTo(STOP_CONDITION);
        assertThat(retryPolicy.isThrowing()).isEqualTo(THROWING);
    }

    @Test
    void canCreateInstanceOfRetryPolicyViaBuilder() {
        final RetryPolicyBuilder<Integer> builder = RetryPolicy.builder();
        final RetryPolicy<Integer> retryPolicy = builder.withInterval(INTERVAL)
                .withTimeout(TIMEOUT)
                .withMaximumExecutions(MAXIMUM_EXECUTIONS)
                .ignoreWhen(IGNORABLE_EXCEPTION)
                .retryUntil(STOP_CONDITION)
                .throwing(true)
                .build();
        assertThat(retryPolicy).isNotNull();
        assertThat(retryPolicy.getInterval()).isEqualTo(INTERVAL);
        assertThat(retryPolicy.getTimeout()).isEqualTo(TIMEOUT);
        assertThat(retryPolicy.getMaximumExecutions()).isEqualTo(MAXIMUM_EXECUTIONS);
        assertThat(retryPolicy.getIgnorableException()).isEqualTo(IGNORABLE_EXCEPTION);
        assertThat(retryPolicy.getStopCondition()).isEqualTo(STOP_CONDITION);
        assertThat(retryPolicy.isThrowing()).isEqualTo(THROWING);
    }
}