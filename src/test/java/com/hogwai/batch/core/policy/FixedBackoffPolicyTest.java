package com.hogwai.batch.core.policy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FixedBackoffPolicyTest {

    @Test
    void backoffShouldSleepForApproximatelyTheConfiguredInterval() throws InterruptedException {
        FixedBackoffPolicy policy = new FixedBackoffPolicy(100);

        long start = System.currentTimeMillis();
        policy.backoff(1);
        long elapsed = System.currentTimeMillis() - start;

        assertThat(elapsed).isGreaterThanOrEqualTo(80);
        assertThat(elapsed).isLessThan(300);
    }

    @Test
    void backoffWithZeroIntervalShouldReturnQuickly() throws InterruptedException {
        FixedBackoffPolicy policy = new FixedBackoffPolicy(0);

        long start = System.currentTimeMillis();
        policy.backoff(1);
        long elapsed = System.currentTimeMillis() - start;

        assertThat(elapsed).isLessThan(50);
    }
}
