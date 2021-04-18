package io.github.gdiegel.retry;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RetryPolicyBuilderTest {

    @Test
    void canCreateBuilderFromRetryPolicy() {
        final RetryPolicyBuilder<Integer> builder = RetryPolicy.builder();
        assertThat(builder).isNotNull();
    }

    @Test
    void canCreateBuilderFromRetryPolicyBuilder() {
        final RetryPolicyBuilder<Integer> builder = RetryPolicyBuilder.instance();
        assertThat(builder).isNotNull();
    }

}