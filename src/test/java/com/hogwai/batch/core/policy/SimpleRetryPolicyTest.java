package com.hogwai.batch.core.policy;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class SimpleRetryPolicyTest {

    @Test
    void shouldAllowRetryWhenUnderMaxAttempts() {
        SimpleRetryPolicy policy = new SimpleRetryPolicy(3);
        policy.registerRetryableException(IllegalStateException.class);
        assertThat(policy.shouldRetry(new IllegalStateException(), 1)).isTrue();
        assertThat(policy.shouldRetry(new IllegalStateException(), 2)).isTrue();
    }

    @Test
    void shouldDenyRetryWhenAtMaxAttempts() {
        SimpleRetryPolicy policy = new SimpleRetryPolicy(3);
        policy.registerRetryableException(IllegalStateException.class);
        assertThat(policy.shouldRetry(new IllegalStateException(), 3)).isFalse();
    }

    @Test
    void shouldDenyRetryForNonRetryableException() {
        SimpleRetryPolicy policy = new SimpleRetryPolicy(3);
        policy.registerRetryableException(IllegalStateException.class);
        assertThat(policy.shouldRetry(new NullPointerException(), 1)).isFalse();
    }

    @Test
    void shouldRetryAnyExceptionWhenNoSpecificExceptionsRegistered() {
        SimpleRetryPolicy policy = new SimpleRetryPolicy(3);
        assertThat(policy.shouldRetry(new RuntimeException(), 1)).isTrue();
    }
}
