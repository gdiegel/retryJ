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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultRetryTest extends BaseTest {

    @Test
    void shouldThrowNullPointerExceptionWhenRetryPolicyIsNull() {
        assertThatThrownBy(() -> Retry.with(null)).isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowNullPointerExceptionWhenCallableIsNull() {
        assertThatThrownBy(() -> RETRY.call(null)).isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowNullPointerExceptionWhenCreatedByConstructorAndRetryPolicyIsNullAndCall() {
        DefaultRetry<Integer> defaultRetry = new DefaultRetry<>(null);
        assertThatThrownBy(() -> defaultRetry.call(IDEMPOTENT_CALLABLE)).isExactlyInstanceOf(NullPointerException.class);
    }
}