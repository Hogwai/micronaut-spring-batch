package com.hogwai.batch.core.policy;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class SimpleSkipPolicyTest {

    @Test
    void shouldAllowSkipWhenUnderLimit() {
        SimpleSkipPolicy policy = new SimpleSkipPolicy(3);
        policy.registerSkippableException(IllegalArgumentException.class);
        assertThat(policy.shouldSkip(new IllegalArgumentException(), 0)).isTrue();
        assertThat(policy.shouldSkip(new IllegalArgumentException(), 2)).isTrue();
    }

    @Test
    void shouldDenySkipWhenAtLimit() {
        SimpleSkipPolicy policy = new SimpleSkipPolicy(2);
        policy.registerSkippableException(IllegalArgumentException.class);
        assertThat(policy.shouldSkip(new IllegalArgumentException(), 2)).isFalse();
    }

    @Test
    void shouldDenySkipForNonSkippableException() {
        SimpleSkipPolicy policy = new SimpleSkipPolicy(10);
        policy.registerSkippableException(IllegalArgumentException.class);
        assertThat(policy.shouldSkip(new NullPointerException(), 0)).isFalse();
    }

    @Test
    void shouldSkipAnyExceptionWhenNoSpecificExceptionsRegistered() {
        SimpleSkipPolicy policy = new SimpleSkipPolicy(5);
        assertThat(policy.shouldSkip(new RuntimeException(), 0)).isTrue();
    }
}
